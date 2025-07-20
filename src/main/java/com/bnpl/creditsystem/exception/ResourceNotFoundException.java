package com.bnpl.creditsystem.exception;

import org.springframework.http.HttpStatus;

public abstract class ResourceNotFoundException extends RuntimeException {
    private final String code;
    private final String error;

    public ResourceNotFoundException(String message, String code, String error) {
        super(message);
        this.code = code;
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public HttpStatus getStatus(){
        return HttpStatus.NOT_FOUND;
    }
}