package com.cinelog.server.exception;

import com.cinelog.server.global.error.BusinessException;
import com.cinelog.server.global.error.ErrorCode;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    public EntityNotFoundException(ErrorCode errorCode,String message) {
        super(errorCode, message);
    }
}
