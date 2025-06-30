package com.bnpl.creditsystem.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PurchaseRequest {

    @NotNull(message = "Client ID cannot be null")
    private Long clientId;

    @NotNull(message = "Purchase amount cannot be null")
    @Positive(message = "Purchase amount must be positive")
    private BigDecimal purchaseAmount;
}