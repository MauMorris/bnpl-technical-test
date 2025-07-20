package com.bnpl.creditsystem.service;

import java.util.UUID;

import com.bnpl.creditsystem.dto.LoanRequest;
import com.bnpl.creditsystem.dto.LoanResponse;

public interface LoanService {

    LoanResponse createLoan(LoanRequest request);
    LoanResponse findLoanById(UUID loanId);
}