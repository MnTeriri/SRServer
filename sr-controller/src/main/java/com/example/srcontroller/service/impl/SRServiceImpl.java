package com.example.srcontroller.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.example.srcommon.config.SRProperties;
import com.example.srcommon.model.SRTask;
import com.example.srcontroller.dao.ISRTaskDao;
import com.example.srcontroller.service.ISRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SRServiceImpl implements ISRService {

    private final ISRTaskDao srTaskDao;

    private final RedisTemplate<String, Object> redisTemplate;

    private final SRProperties srProperties;

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submit(MultipartFile uploadFile, String modelName, Integer scale) throws IOException {
        //1.判定模型是否正确

        //2.判定图片大小是否超出要求（以后加上照片是否已经传过的判定）

        //3.进行处理
        String oldName = uploadFile.getOriginalFilename();//原文件名
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" +
                RandomUtil.randomNumbers(6) + oldName.substring(oldName.lastIndexOf("."));//生成新文件名
        log.debug("使用模型：{}，放大倍率：{}", modelName, scale);
        log.debug("文件原名称：{}，文件现名称：{}", oldName, fileName);
        uploadFile.transferTo(Path.of(srProperties.getInputDir() + "/" + fileName).toFile());//保存文件

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
        //放入Redis
//        redisTemplate.opsForValue().set(task.getTaskId(), task);
        redisTemplate.convertAndSend("sr-task-channel", task);
        //放入消息队列
        rocketMQTemplate.convertAndSend("sr-task-topic", task);

        return taskId;
    }
}
