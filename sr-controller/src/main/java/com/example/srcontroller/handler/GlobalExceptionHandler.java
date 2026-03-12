package com.example.srcontroller.handler;

import com.example.srcommon.exception.SystemException;
import com.example.srcommon.response.ResponseResult;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    public GlobalExceptionHandler() {
        log.debug("创建RestControllerAdvice：{}", this);
    }

    //处理自定义异常
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<String> handlerSystemException(SystemException exception) {
        log.error("发生自定义SystemException异常：{}", exception.getResponseStatus());
        return ResponseResult.error(exception.getResponseStatus());
    }

    @ExceptionHandler(MessagingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<String> handlerMessagingException(MessagingException exception) {
        log.error("发生MessagingException异常：{}", exception.toString());
        return ResponseResult.error();
    }
}
