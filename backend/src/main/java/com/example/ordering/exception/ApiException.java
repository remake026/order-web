package com.example.ordering.exception;

/**
 * 业务异常，由 GlobalExceptionHandler 转成 400 + {"message": "..."}
 */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}
