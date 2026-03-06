package com.example.srcommon.exception;

import com.example.srcommon.response.ResponseCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemException extends RuntimeException {
    private ResponseCode responseStatus;

    public SystemException(ResponseCode responseStatus) {
        super(responseStatus.toString());
        this.responseStatus = responseStatus;
    }

}
