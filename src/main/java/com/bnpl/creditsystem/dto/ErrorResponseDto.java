package com.bnpl.creditsystem.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ErrorResponseDto {
    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
}