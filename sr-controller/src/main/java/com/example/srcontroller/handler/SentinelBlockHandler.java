package com.example.srcontroller.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.example.srcommon.response.ResponseCode;
import com.example.srcommon.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class SentinelBlockHandler {

    public static ResponseEntity<ResponseResult<String>> submitBlockHandler(
            MultipartFile uploadFile,
            String modelName,
            Integer scale,
            BlockException ex) {
        log.error("接口 {} 限流：{}", ex.getRule().getResource(), ex.toRuntimeException().toString());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseResult.error(ResponseCode.INFERENCE_BUSY_ERROR));
    }

}
