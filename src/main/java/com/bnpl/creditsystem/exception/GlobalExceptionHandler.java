package com.bnpl.creditsystem.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.bnpl.creditsystem.dto.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice // Esta anotación permite a la clase interceptar excepciones de toda la aplicación.
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las excepciones de validación de DTOs (cuando @Valid falla).
     * Devuelve un error 400 Bad Request con un mensaje claro de los campos que fallaron.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        final String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "'" + error.getField() + "': " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errorMessage);

        final String code;
        final String error;

        final String path = request.getRequestURI();
        // Asignamos códigos de error según la especificación OpenAPI
        if (path.startsWith("/v1/customers")) {
            code = "APZ000002";
            error = "INVALID_CUSTOMER_REQUEST";
        } else if (path.startsWith("/v1/loans")) {
            code = "APZ000006";
            error = "INVALID_LOAN_REQUEST";
        } else {
            code = "APZ000004"; // Fallback
            error = "INVALID_REQUEST";
        }
        ErrorResponseDto errorResponse = new ErrorResponseDto(code, error, Instant.now().getEpochSecond(), errorMessage, path);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de lógica de negocio personalizadas que extienden de BusinessLogicException.
     * Esto centraliza el manejo de errores de negocio predecibles (edad inválida, crédito insuficiente, etc.).
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessLogicException(BusinessLogicException ex, HttpServletRequest request) {
        log.warn("Business logic error [{} - {}]: {}", ex.getStatus(), ex.getError(), ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(
            ex.getCode(), 
            ex.getError(), 
            Instant.now().getEpochSecond(), 
            ex.getMessage(), 
            request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

     @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getCode(), ex.getError(), Instant.now().getEpochSecond(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
}