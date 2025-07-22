package com.bnpl.creditsystem.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.bnpl.creditsystem.dto.InstallmentResponse;
import com.bnpl.creditsystem.dto.LoanResponse;
import com.bnpl.creditsystem.entity.Loan;

@Component
public class LoanMapper {

    public LoanResponse toLoanResponse(Loan loan) {
        List<InstallmentResponse> installmentResponses = loan.getInstallments().stream()
                .map(installment -> new InstallmentResponse(
                        installment.getAmount(),
                        installment.getScheduledPaymentDate(),
                        installment.getStatus()
                ))
                .toList();
        LoanResponse.PaymentPlan paymentPlan = new LoanResponse.PaymentPlan(
            loan.getCommission(),
            installmentResponses
            );
        
        return new LoanResponse(
        loan.getId(),
        loan.getCustomer().getId(),
        loan.getLoanAmount(),
        loan.getStatus(),
        loan.getCreatedAt(),
        paymentPlan
        );
    }
}