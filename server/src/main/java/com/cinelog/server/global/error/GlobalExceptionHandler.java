package com.cinelog.server.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cinelog.server.dto.ApiResult;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResult<?>> handleBusinessException(BusinessException e) {
        log.warn("Business Exception: {}", e.getMessage()); // 의도된 예외이므로 warn 레벨
        ErrorCode errorCode = e.getErrorCode();
        
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResult.error(e.getMessage(),errorCode.getStatus()));//errorcode의 메세지보다 error에 담긴 메시지 우선
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResult<?>> handleValidationException(MethodArgumentNotValidException e) {//입력 유효성 검사
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("Validation Failed: {}", errorMessage);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error(errorMessage, HttpStatus.BAD_REQUEST));
    }

  
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})//도메인 규칙 위반 에러
    protected ResponseEntity<ApiResult<?>> handleDomainException(RuntimeException e) {
        log.warn("Domain Validation Failed: {}", e.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)//일단 400처리
                .body(ApiResult.error(e.getMessage(), HttpStatus.BAD_REQUEST));
    }


    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResult<?>> handleException(Exception e) {
        log.error("Internal Server Error", e); 
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}