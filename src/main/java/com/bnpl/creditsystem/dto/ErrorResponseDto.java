package com.bnpl.creditsystem.dto;

import java.time.Instant;
import lombok.Data;

@Data
public class ErrorResponseDto {
    private final long timestamp = Instant.now().getEpochSecond();

    private final int status;
    private final String message;
    private final String path;
    
    private String code;
    private String error;
}