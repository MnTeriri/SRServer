package com.example.srinference.service.impl;

import com.example.srcommon.model.SRTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RocketMQMessageListener(
        topic = "sr-task-topic",
        consumerGroup = "sr-task-consumer-group",
        consumeThreadNumber = 1, //初始线程数
        consumeThreadMax = 1 //最大线程数
)
public class SRTaskListener implements RocketMQListener<SRTask> {
    @Override
    public void onMessage(SRTask task) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug(task.toString());
    }
}