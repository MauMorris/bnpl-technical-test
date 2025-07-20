package com.bnpl.creditsystem.exception;

import org.springframework.http.HttpStatus;

public class InsufficientCreditException extends BusinessLogicException {
    // Según la spec de OpenAPI, un error en la creación de un préstamo debe devolver
    // un 400 Bad Request con el código APZ000006.
    public InsufficientCreditException(String message) {
        super(message, "APZ000006", "INVALID_LOAN_REQUEST", HttpStatus.BAD_REQUEST);
    }
}