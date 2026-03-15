package com.cinelog.server.exception.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cinelog.server.global.error.BusinessException;
import com.cinelog.server.global.error.ErrorCode;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
    public ForbiddenException(ErrorCode errorCode,String message) {
        super(errorCode, message);
    }
}