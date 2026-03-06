package com.example.srcontroller.controller;

import com.example.srcommon.response.ResponseResult;
import com.example.srcontroller.service.ISRService;
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

@Slf4j
@RestController
@RequestMapping("/sr")
@Tag(name = "SR接口")
@RequiredArgsConstructor
public class SRController {

    private final ISRService srService;

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
        srService.submit(uploadFile, modelName, scale);
        return ResponseEntity.ok(ResponseResult.ok());
    }
}
