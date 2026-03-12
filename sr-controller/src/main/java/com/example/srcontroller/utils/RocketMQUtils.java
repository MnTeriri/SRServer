package com.example.srcontroller.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RocketMQUtils {
    @Value("${rocketmq.consumer.group}")
    private String consumerGroup;

    @Value("${rocketmq.max-queue-count}")
    private long maxQueueCount;

    private final DefaultMQAdminExt adminExt;

    public boolean isQueueOverloaded() {
        try {
            long backlog = getDiffTotal();
            log.debug("当前RocketMQ队列任务数量：{}", backlog);
            return backlog >= maxQueueCount;
        } catch (Exception e) {
            log.error(e.getMessage());
            return true;
        }
    }

    public long getDiffTotal() throws Exception {
        ConsumeStats consumeStats = adminExt.examineConsumeStats(consumerGroup);
        return consumeStats.computeTotalDiff();
    }
}