package com.example.mockvoting.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final String message;
    private final HttpStatus status;
    private final String code;

    public CustomException(String message) {
        this(message, HttpStatus.BAD_REQUEST, null);
    }

    public CustomException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public CustomException(String message, HttpStatus status, String code) {
        super(message);
        this.message = message;
        this.status = status;
        this.code = code;
    }
}