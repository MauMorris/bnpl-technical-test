package com.bnpl.creditsystem.exception;

import org.springframework.http.HttpStatus;

public class InvalidAgeException extends BusinessLogicException {
    // Corresponde al error 400 en POST /customers
    public InvalidAgeException(String message) {
        super(message, "APZ000002", "INVALID_CUSTOMER_REQUEST", HttpStatus.BAD_REQUEST);
    }
}