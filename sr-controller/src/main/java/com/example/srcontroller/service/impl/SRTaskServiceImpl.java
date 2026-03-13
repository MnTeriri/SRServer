package com.example.srcontroller.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.srcommon.config.SRProperties;
import com.example.srcommon.exception.SystemException;
import com.example.srcommon.model.SRTask;
import com.example.srcommon.response.ResponseCode;
import com.example.srcontroller.dao.ISRTaskDao;
import com.example.srcontroller.service.ISRTaskService;
import com.example.srcontroller.utils.RocketMQUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SRTaskServiceImpl implements ISRTaskService {

    private final ISRTaskDao srTaskDao;

    private final SRProperties properties;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RocketMQTemplate rocketMQTemplate;

    private final RocketMQUtils rocketMQUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submit(MultipartFile uploadFile, String modelName, Integer scale) {
        //1.判定模型是否正确
        List<SRProperties.ModelConfig> modelConfigs = properties.getModels().get(modelName);
        if (modelConfigs == null ||
                modelConfigs.stream().noneMatch(modelConfig -> Objects.equals(modelConfig.getScale(), scale))) {
            throw new SystemException(ResponseCode.NO_SUCH_MODEL_ERROR);
        }

        //2.判定RocketMQ队列是否达到上限
        if (rocketMQUtils.isQueueOverloaded()) {
            throw new SystemException(ResponseCode.MQ_LIMIT_ERROR);
        }

        //3.判定图片大小是否超出要求（以后加上照片是否已经传过的判定）


        //4.进行处理
        String oldName = uploadFile.getOriginalFilename();//原文件名
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" +
                RandomUtil.randomNumbers(6) + oldName.substring(oldName.lastIndexOf("."));//生成新文件名
        log.debug("使用模型：{}，放大倍率：{}", modelName, scale);
        log.debug("文件原名称：{}，文件现名称：{}", oldName, fileName);
        try {
            uploadFile.transferTo(Path.of(properties.getImageInputDir() + "/" + fileName).toFile());//保存文件
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SystemException(ResponseCode.ERROR);
        }

        String taskId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + RandomUtil.randomNumbers(6);
        SRTask task = new SRTask()
                .setTaskId(taskId)
                .setModelName(modelName)
                .setScale(scale)
                .setInputFile(fileName)
                .setState(SRTask.SRTaskState.CREATE)
                .setCreateTime(LocalDateTime.now());

        //任务添加到数据库
        srTaskDao.insert(task);
        //放入Redis并发送消息，缓存5分钟过期
        redisTemplate.opsForValue().set(task.getTaskId(), task, Duration.ofMinutes(5));
        redisTemplate.convertAndSend("sr-task-channel", task);
        //放入消息队列
        rocketMQTemplate.convertAndSend("sr-task-topic", task);

        return taskId;
    }

    @Override
    public List<Map<String, Object>> getModelList() {
        Map<String, List<SRProperties.ModelConfig>> models = properties.getModels();
        List<Map<String, Object>> list = new ArrayList<>();

        models.forEach((modelName, modelConfigs) -> {
            Map<String, Object> map = new HashMap<>();
            List<Integer> scales = modelConfigs.stream().map(SRProperties.ModelConfig::getScale).toList();
            map.put("modelName", modelName);
            map.put("scale", scales);
            list.add(map);
        });
        return list;
    }

    @Override
//    @Cacheable(cacheNames = "sr-task", key = "#taskId")//懒得改cache key了
    public SRTask searchSRTaskByTaskId(String taskId) {
        //先找缓存
        SRTask task = (SRTask) redisTemplate.opsForValue().get(taskId);
        if (task != null) {
            log.debug("Redis缓存命中：{}", task);
            return task;
        }
        //缓存没有查数据库
        task = srTaskDao.selectOne(new QueryWrapper<SRTask>().eq("task_id", taskId));
        if (task == null) {
            throw new SystemException(ResponseCode.NO_SUCH_TASK_ERROR);
        }
        //更新缓存
        redisTemplate.opsForValue().set(task.getTaskId(), task, Duration.ofMinutes(5));
        return task;
    }

    @Override
    public Resource downloadTaskImage(String taskId) {
        SRTask srTask = searchSRTaskByTaskId(taskId);
        //任务未完成
        if (srTask.getState() == SRTask.SRTaskState.CREATE ||
                srTask.getState() == SRTask.SRTaskState.RUNNING) {
            throw new SystemException(ResponseCode.TASK_NOT_FINISH_ERROR);
        }

        //任务失败
        if (srTask.getState() == SRTask.SRTaskState.FAIL) {
            throw new SystemException(ResponseCode.TASK_FAIL_ERROR);
        }

        String path = properties.getImageOutputDir() + "/" + srTask.getOutputFile();
        return new FileSystemResource(path);
    }
}
