package com.bnpl.creditsystem.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class BusinessLogicException extends RuntimeException {
    private final String code;
    private final String error;
    private final HttpStatus status;

    protected BusinessLogicException(String message, String code, String error, HttpStatus status) {
        super(message);
        this.code = code;
        this.error = error;
        this.status = status;
    }
}