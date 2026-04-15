package com.example.srcontroller.sse;

import com.example.srcommon.model.SRTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SRTaskSSEManager {
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter register(String taskId) {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L); // 3分钟
        emitterMap.put(taskId, emitter);
        log.debug("SSE任务：{} Create", taskId);

        emitter.onCompletion(() -> {
            emitterMap.remove(taskId);
            log.debug("SSE任务：{} onCompletion", taskId);
        });
        emitter.onTimeout(() -> {
            emitterMap.remove(taskId);
            log.debug("SSE任务：{} onTimeout", taskId);
        });
        emitter.onError((e) -> {
            emitterMap.remove(taskId);
            log.debug("SSE任务：{} onError：{}", taskId, e.toString());
        });

        return emitter;
    }

    public void send(String taskId, SRTask srTask) {
        SseEmitter emitter = emitterMap.get(taskId);

        //没有监听则直接返回
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("sr-task-state-update")
                    .data(srTask));

            if (srTask.getState() == SRTask.SRTaskState.FINISH) {
                emitter.complete();
            }

        } catch (IOException e) {
            emitterMap.remove(taskId);
            emitter.complete();
        }
    }
}
