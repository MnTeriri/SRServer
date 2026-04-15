package com.example.srcontroller.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.example.srcommon.model.SRModelInfo;
import com.example.srcommon.model.SRTask;
import com.example.srcommon.response.ResponseResult;
import com.example.srcontroller.handler.SentinelBlockHandler;
import com.example.srcontroller.service.ISRTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sr")
@RequiredArgsConstructor
@Tag(name = "SR接口")
public class SRTaskController {

    private final ISRTaskService srTaskService;

    @Operation(summary = "超分任务提交接口")
    @Parameters({
            @Parameter(name = "modelName", description = "模型名称", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "scale", description = "放大倍率", required = true, in = ParameterIn.QUERY),
    })
    @PostMapping("/task/submit/{modelName}/{scale}")
    @SentinelResource(value = "submitTask", blockHandler = "submitBlockHandler", blockHandlerClass = SentinelBlockHandler.class)
    public ResponseEntity<ResponseResult<String>> submit(
            @RequestParam("file") MultipartFile uploadFile,
            @PathVariable String modelName,
            @PathVariable Integer scale
    ) {
        String taskId = srTaskService.submit(uploadFile, modelName, scale);
        return ResponseEntity.ok(ResponseResult.ok(taskId));
    }

    @Operation(summary = "获取可用模型")
    @GetMapping("/models")
    public ResponseEntity<ResponseResult<List<SRModelInfo>>> getModelList() {
        return ResponseEntity.ok(ResponseResult.ok(srTaskService.getModelList()));
    }

    @Operation(summary = "获取超分任务（分页）")
    @Parameters({
            @Parameter(name = "currentPage", description = "当前页面", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "页面大小", required = true, in = ParameterIn.QUERY),
    })
    @PostMapping("/task/searchSRTaskList")
    public ResponseEntity<ResponseResult<List<SRTask>>> searchSRTaskList(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return ResponseEntity.ok(ResponseResult.ok(srTaskService.searchSRTaskList(currentPage, pageSize)));
    }

    @Operation(summary = "删除超分任务")
    @Parameters({
            @Parameter(name = "taskId", description = "任务ID", required = true, in = ParameterIn.QUERY),
    })
    @PostMapping("/task/deleteSRTask")
    public ResponseEntity<ResponseResult<String>> deleteSRTask(String taskId) {
        srTaskService.deleteSRTaskByTaskId(taskId);
        return ResponseEntity.ok(ResponseResult.ok());
    }

    @Operation(summary = "下载图片")
    @GetMapping("/task/download/{taskId}")
    public ResponseEntity<Resource> download(@PathVariable String taskId){
        Resource resource = srTaskService.downloadTaskImage(taskId);
        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}
