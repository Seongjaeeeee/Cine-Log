package com.cinelog.server.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cinelog.server.global.error.BusinessException;
import com.cinelog.server.global.error.ErrorCode;

public class DuplicateException extends BusinessException {
    public DuplicateException(ErrorCode errorCode) {
        super(errorCode);
    }
    public DuplicateException(ErrorCode errorCode,String message) {
        super(errorCode,message);
    }
}
