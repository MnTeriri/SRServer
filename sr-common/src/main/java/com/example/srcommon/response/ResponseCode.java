package com.example.srcommon.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ResponseCode {
    OK(200, "操作成功"),
    ERROR(100000, "操作失败"),
    NO_SUCH_TASK_ERROR(100001, "没有该任务"),
    NO_SUCH_MODEL_ERROR(100002, "没有该超分模型或对应放大倍率"),
    IMAGE_TOO_BIG_ERROR(100003, "图片分辨率过大"),
    INFERENCE_ERROR(100004, "推理失败"),
    TASK_BUSY_ERROR(100005, "服务繁忙"),
    MQ_BUSY_ERROR(100006, "队列达到上限"),
    AUTHORIZED_ERROR(401, "没有权限，需要登录"),
    FORBIDDEN_ERROR(403, "权限不够，被拒绝"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
