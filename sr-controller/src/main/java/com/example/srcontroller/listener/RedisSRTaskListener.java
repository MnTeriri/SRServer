package com.example.srcontroller.listener;

import com.alibaba.fastjson2.JSON;
import com.example.srcommon.model.SRTask;
import com.example.srcontroller.sse.SRTaskSSEManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSRTaskListener implements MessageListener {

    private final SRTaskSSEManager sseManager;

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        SRTask srTask = JSON.parseObject(new String(message.getBody()), SRTask.class);
        log.debug("收到任务状态变化：{}", srTask);
        //发送给SSE
        sseManager.send(srTask.getTaskId(), srTask);
    }
}
