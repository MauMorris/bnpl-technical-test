package com.bnpl.creditsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bnpl.creditsystem.entity.LoanStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private UUID id;
    private UUID customerId;
    private BigDecimal amount;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private PaymentPlan paymentPlan;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentPlan {
        private BigDecimal commissionAmount;
        private List<InstallmentResponse> installments;
    }
}