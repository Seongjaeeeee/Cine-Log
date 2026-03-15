package com.cinelog.server.exception.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cinelog.server.global.error.BusinessException;
import com.cinelog.server.global.error.ErrorCode;

public class UnAuthorizedException extends BusinessException {
    public UnAuthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
    public UnAuthorizedException(ErrorCode errorCode,String message) {
        super(errorCode,message);
    }
}