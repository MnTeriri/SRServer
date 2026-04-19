package com.example.srcontroller.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.srcommon.config.SRProperties;
import com.example.srcommon.exception.SystemException;
import com.example.srcommon.model.ImageMeta;
import com.example.srcommon.model.SRModelInfo;
import com.example.srcommon.model.SRTask;
import com.example.srcommon.response.ResponseCode;
import com.example.srcommon.utils.ImageUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SRTaskServiceImpl implements ISRTaskService {

    private final ISRTaskDao srTaskDao;

    private final SRProperties properties;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RocketMQTemplate rocketMQTemplate;

    private final RocketMQUtils rocketMQUtils;

    private String createTask(SRTask task) {
        String taskId = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(6);

        task.setTaskId(taskId)
                .setState(SRTask.SRTaskState.CREATE)
                .setCreateTime(LocalDateTime.now());

        log.debug("创建任务：{}", task);

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
    @Transactional(rollbackFor = Exception.class)
    public String submit(MultipartFile uploadFile, String modelName, Integer scale) {
        //1.判定模型是否正确
        List<SRProperties.ModelConfig> modelConfigs = properties.getModels().get(modelName);
        if (modelConfigs == null ||
                modelConfigs.stream().noneMatch(modelConfig -> Objects.equals(modelConfig.getScale(), scale))) {
            throw new SystemException(ResponseCode.NO_SUCH_MODEL_ERROR);
        }

        try {
            //2.判定图片大小是否超出要求
            ImageMeta meta = ImageUtils.getImageMeta(uploadFile.getInputStream(), uploadFile.getSize());
            log.info("图片信息: {}x{}, {}", meta.getWidth(), meta.getHeight(), meta.formatSize());
            if (meta.getHeight() > 3000 || meta.getWidth() > 3000 || meta.toMB() > 5.0) {
                throw new SystemException(ResponseCode.IMAGE_SIZE_LIMIT_ERROR);
            }

            //3.查是否已有“同图 + 同模型 + 同倍率”的任务，注意同任务如果失败则尝试重建（暂时不实现）


            //4.判定RocketMQ队列是否达到上限
            if (rocketMQUtils.isQueueOverloaded()) {
                throw new SystemException(ResponseCode.MQ_LIMIT_ERROR);
            }

            //5.查是否已有相同原图，用于复用 inputFile
            String md5 = SecureUtil.md5(uploadFile.getInputStream());
            String inputFile = searchInputFileByMd5(md5);
            if (inputFile != null) {
                log.debug("复用已有输入图片, 文件名称：{}, 文件MD5：{}", inputFile, md5);
            } else {
                String oldName = uploadFile.getOriginalFilename();//原文件名
                String suffix = oldName.substring(oldName.lastIndexOf(".")).toLowerCase();//后缀

                //生成新文件名
                inputFile = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + "_"
                        + RandomUtil.randomNumbers(6)
                        + suffix;

                //保存文件
                uploadFile.transferTo(Path.of(properties.getImageInputDir(), inputFile).toFile());
                log.debug("保存新输入图片，文件原名称：{}，文件现名称：{}，文件MD5：{}", oldName, inputFile, md5);
            }

            SRTask task = new SRTask()
                    .setModelName(modelName)
                    .setScale(scale)
                    .setInputFile(inputFile)
                    .setInputFileMD5(md5)
                    .setInputMeta(meta);

            //6.创建任务
            return createTask(task);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new SystemException(ResponseCode.ERROR);
        }
    }

    @Override
    public List<SRModelInfo> getModelList() {
        Map<String, List<SRProperties.ModelConfig>> models = properties.getModels();
        List<SRModelInfo> list = new ArrayList<>();

        models.forEach((modelName, modelConfigs) -> {
            List<Integer> scales = modelConfigs.stream().map(SRProperties.ModelConfig::getScale).toList();
            list.add(new SRModelInfo(modelName, scales));
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
    public String searchInputFileByMd5(String md5) {
        SRTask task = srTaskDao.selectOne(
                new QueryWrapper<SRTask>()
                        .select("input_file")
                        .eq("input_file_md5", md5)
                        .orderByDesc("id")
                        .last("LIMIT 1")
        );
        return task == null ? null : task.getInputFile();
    }

    @Override
    public SRTask searchSRTaskByNameAndScale(String modelName, Integer scale) {
        return null;
    }

    @Override
    public List<SRTask> searchSRTaskList(Integer currentPage, Integer pageSize) {
        Page<SRTask> page = new Page<>(currentPage, pageSize);
        QueryWrapper<SRTask> queryWrapper = new QueryWrapper<SRTask>()
                .orderByDesc("id");
        return srTaskDao.selectList(page, queryWrapper);
//        return srTaskDao.searchSRTaskList((currentPage - 1) * pageSize, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSRTaskByTaskId(String taskId) {
        //1.查询任务是否存在
        log.debug("查询是否存在任务：{}", taskId);
        SRTask task = srTaskDao.selectOne(new QueryWrapper<SRTask>().eq("task_id", taskId));
        if (task == null) {
            throw new SystemException(ResponseCode.NO_SUCH_TASK_ERROR);
        }

        // 2. 只有 FINISH / FAIL 才允许删除
        if (task.getState() != SRTask.SRTaskState.FINISH && task.getState() != SRTask.SRTaskState.FAIL) {
            throw new SystemException(ResponseCode.TASK_NOT_FINISH_ERROR);
        }

        log.debug("任务状态：{}，删除任务 {} 的 Redis 缓存和数据库记录", task.getState(), taskId);
        // 3. 删除 Redis 缓存和数据库记录
        redisTemplate.delete(taskId);
        int row = srTaskDao.delete(new QueryWrapper<SRTask>().eq("task_id", taskId));
        if (row != 1) {
            throw new SystemException(ResponseCode.NO_SUCH_TASK_ERROR);
        }

        try {
            // 4. 删除输出图片（注意判空，因为当任务失败时是没有输出图片的）
            if (task.getOutputFile() == null) {
                log.debug("任务 {} 的输出图片不存在，不执行删除", taskId);
            } else {
                log.debug("删除任务 {} 的输出图片", taskId);
                Files.deleteIfExists(Path.of(properties.getImageOutputDir(), task.getOutputFile()));
            }

            // 5. 判断输入图片是否还被其他任务使用
            boolean isUseInputFile = srTaskDao.selectCount(new QueryWrapper<SRTask>().eq("input_file", task.getInputFile())) > 0;
            if (isUseInputFile) {
                log.debug("任务 {} 的输入图片还在使用，不执行删除", taskId);
            } else {
                log.debug("删除任务 {} 的输入图片", taskId);
                Files.deleteIfExists(Path.of(properties.getImageInputDir(), task.getInputFile()));
            }
        } catch (IOException e) {
            log.error("删除图片失败, taskId={}", taskId);
            log.error(e.getMessage(), e);
        }
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
