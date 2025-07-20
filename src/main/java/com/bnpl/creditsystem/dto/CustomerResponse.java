package com.bnpl.creditsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private UUID id;
    private BigDecimal creditLineAmount;
    private BigDecimal availableCreditLineAmount;
    private LocalDateTime createdAt;
}