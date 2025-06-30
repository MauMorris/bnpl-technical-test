package com.bnpl.creditsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.bnpl.creditsystem.dto.ErrorResponseDto;

@ControllerAdvice // Esta anotación le dice a Spring que esta clase vigilará todos los controladores.
public class GlobalExceptionHandler {

    // Este método se ejecutará CADA VEZ que un controlador lance una IllegalStateException o IllegalArgumentException.
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponseDto> handleBusinessException(RuntimeException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}