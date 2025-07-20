package com.bnpl.creditsystem.dto;

import lombok.Data;

@Data
public class ErrorResponseDto {
    private final String code;
    private final String error;
    private final long timestamp;

    private final String message;
    private final String path;    
}