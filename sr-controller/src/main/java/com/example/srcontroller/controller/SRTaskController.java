package com.example.srcontroller.controller;

import com.example.srcommon.response.ResponseResult;
import com.example.srcontroller.service.ISRTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @PostMapping("/submit/{modelName}/{scale}")
    public ResponseEntity<ResponseResult<String>> submit(
            @RequestParam("file") MultipartFile uploadFile,
            @PathVariable String modelName,
            @PathVariable Integer scale
    ) throws IOException {
        String taskId = srTaskService.submit(uploadFile, modelName, scale);
        return ResponseEntity.ok(ResponseResult.ok(taskId));
    }

    @Operation(summary = "获取可用模型")
    @GetMapping("/models")
    public ResponseEntity<ResponseResult<List<Map<String, Object>>>> getModelList() {
        return ResponseEntity.ok(ResponseResult.ok(srTaskService.getModelList()));
    }
}
