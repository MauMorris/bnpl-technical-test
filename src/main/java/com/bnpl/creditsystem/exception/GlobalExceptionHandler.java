package com.bnpl.creditsystem.exception;

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
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "'" + error.getField() + "': " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errorMessage);

        ErrorResponseDto errorResponse = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), errorMessage, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones por argumentos inválidos (ej. edad fuera de rango) o estado inválido (ej. crédito insuficiente).
     * Devuelve un error 400 Bad Request o 409 Conflict según corresponda.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponseDto> handleBusinessExceptions(RuntimeException ex, HttpServletRequest request) {
        // Usamos 409 Conflict para estados inválidos (crédito insuficiente) y 400 para el resto.
        HttpStatus status = (ex instanceof IllegalStateException) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        log.warn("Business logic error [{}]: {}", status, ex.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(status.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, status);
    }
}