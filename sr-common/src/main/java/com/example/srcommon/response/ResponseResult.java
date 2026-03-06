package com.example.srcommon.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseResult<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ResponseResult<T> ok() {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(ResponseCode.OK.getCode());
        result.setMessage(ResponseCode.OK.getMessage());
        result.setData(null);
        return result;
    }

    public static <T> ResponseResult<T> ok(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(ResponseCode.OK.getCode());
        result.setMessage(ResponseCode.OK.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> ResponseResult<T> ok(T data, String msg) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(ResponseCode.OK.getCode());
        result.setMessage(msg);
        result.setData(data);
        return result;
    }

    public static <T> ResponseResult<T> error() {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(ResponseCode.ERROR.getCode());
        result.setMessage(ResponseCode.ERROR.getMessage());
        result.setData(null);
        return result;
    }

    public static <T> ResponseResult<T> error(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(ResponseCode.ERROR.getCode());
        result.setMessage(ResponseCode.ERROR.getMessage());
        result.setData(data);
        return result;
    }

    public static ResponseResult<String> error(ResponseCode status) {
        return new ResponseResult<>(status.getCode(), status.getMessage(), null);
    }

    public static ResponseResult<String> error(Integer code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }
}
