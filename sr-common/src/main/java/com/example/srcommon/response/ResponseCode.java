package com.example.srcommon.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ResponseCode {
    OK(200, "操作成功"),
    ERROR(100000, "操作失败"),
    NO_TASK_ERROR(100001, "没有该任务"),
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
