package com.bnpl.creditsystem.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LoanRequest {

    @NotNull(message = "Client ID cannot be null")
    private UUID customerId;

    @NotNull(message = "Loan amount cannot be null")
    @Positive(message = "Loan amount must be positive")
    private BigDecimal amount;
}