package com.example.srcontroller.controller;

import com.example.srcommon.model.SRTask;
import com.example.srcontroller.service.ISRTaskService;
import com.example.srcontroller.sse.SRTaskSSEManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
@Tag(name = "SSE查询接口")
public class SSEController {

    private final ISRTaskService srTaskService;

    private final SRTaskSSEManager sseManager;

    @Operation(summary = "超分任务查询接口")
    @Parameters({
            @Parameter(name = "taskId", description = "任务ID", required = true, in = ParameterIn.QUERY),
    })
    @GetMapping("/task/{taskId}")
    public SseEmitter stream(@PathVariable String taskId) throws IOException {
        SRTask task = srTaskService.searchSRTaskByTaskId(taskId);
        if (task.getState() == SRTask.SRTaskState.FINISH
                || task.getState() == SRTask.SRTaskState.FAIL) {
            //状态为完成时没必要再监听，直接结束
            SseEmitter emitter = new SseEmitter(0L);
            emitter.send(SseEmitter.event()
                    .name("sr-task-state-update")
                    .data(task));
            emitter.complete();
            return emitter;
        }
        SseEmitter emitter = sseManager.register(taskId);
        sseManager.send(task.getTaskId(), task);
        return emitter;
    }
}
