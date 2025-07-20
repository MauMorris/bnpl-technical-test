package com.bnpl.creditsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.bnpl.creditsystem.entity.InstallmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentResponse {
    private BigDecimal amount;
    private LocalDate scheduledPaymentDate;
    private InstallmentStatus status;
}