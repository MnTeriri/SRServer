package com.example.srinference.listener;

import com.example.srcommon.config.SRProperties;
import com.example.srcommon.model.ImageMeta;
import com.example.srcommon.model.SRTask;
import com.example.srcommon.utils.ImageUtils;
import com.example.srinference.core.ModelRegistry;
import com.example.srinference.core.SRModel;
import com.example.srinference.dao.ISRTaskDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
@RocketMQMessageListener(
        topic = "sr-task-topic",
        consumerGroup = "sr-task-consumer-group",
        consumeThreadNumber = 1, //初始线程数
        consumeThreadMax = 1 //最大线程数
)
public class SRTaskListener implements RocketMQListener<SRTask> {

    private final ISRTaskDao srTaskDao;

    private final RedisTemplate<String, Object> redisTemplate;

    private final SRProperties properties;

    private final ModelRegistry modelRegistry;

    @Override
    public void onMessage(SRTask task) {
        log.debug("收到任务：{}", task);

        //1.更新任务状态为RUNNING
        task.setState(SRTask.SRTaskState.RUNNING);
        //保存到数据库
        srTaskDao.updateById(task);
        //放入Redis并发送消息，缓存5分钟过期
        redisTemplate.opsForValue().set(task.getTaskId(), task, Duration.ofMinutes(5));
        redisTemplate.convertAndSend("sr-task-channel", task);

        //2.执行超分任务（超时或者报错直接变成失败任务）
        log.debug("正在运行任务：{}", task);

        SRModel model = modelRegistry.get(task.getModelName(), task.getScale());

        try {
            //处理输出照片名称
            String[] split = task.getInputFile().split("\\.");
            task.setOutputFile(split[0] + "_" + task.getModelName() + "x" + task.getScale() + "." + split[1]);

            String inputPath = properties.getImageInputDir() + "/" + task.getInputFile();
            String outputPath = properties.getImageOutputDir() + "/" + task.getOutputFile();

            //推理
            model.infer(inputPath, outputPath);

            //获取输出照片大小
            ImageMeta meta = ImageUtils.getImageMeta(outputPath);
            task.setOutputMeta(meta);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            task.setState(SRTask.SRTaskState.FAIL)
                    .setOutputFile(null)
                    .setFinishTime(LocalDateTime.now());
            log.error("任务执行失败：{}", task);

            //保存到数据库
            srTaskDao.updateById(task);
            //放入Redis并发送消息，缓存5分钟过期
            redisTemplate.opsForValue().set(task.getTaskId(), task, Duration.ofMinutes(5));
            redisTemplate.convertAndSend("sr-task-channel", task);
            return;
        }

        //3.更新任务状态为FINISH
        task.setState(SRTask.SRTaskState.FINISH).setFinishTime(LocalDateTime.now());
        //保存到数据库
        srTaskDao.updateById(task);
        //放入Redis并发送消息，缓存5分钟过期
        redisTemplate.opsForValue().set(task.getTaskId(), task, Duration.ofMinutes(5));
        redisTemplate.convertAndSend("sr-task-channel", task);

        log.debug("任务结束：{}", task);
    }
}