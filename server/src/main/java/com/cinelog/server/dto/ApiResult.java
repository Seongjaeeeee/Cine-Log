package com.cinelog.server.dto;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiResult<T> {
    private final boolean success;
    private final T response;
    private final ApiError error;

    private ApiResult(boolean success, T response, ApiError error) {
        this.success = success;
        this.response = response;
        this.error = error;
    }

    // 성공 시 호출
    public static <T> ApiResult<T> success(T response) {
        return new ApiResult<>(true, response, null);
    }
    public static <T> ApiResult<T> success() {//내용 없을때
        return new ApiResult<>(true, null, null);
    }
    // 실패 시 호출
    public static ApiResult<?> error(String message, HttpStatus status) {
        return new ApiResult<>(false, null, new ApiError(message, status.value()));
    }
    @Getter
    public static class ApiError {// 내부 에러 클래스
        private final String message;
        private final int status;
        
        public ApiError(String message, int status) {
            this.message = message;
            this.status = status;
        }
    }
}