package com.bnpl.creditsystem.exception;

public class LoanNotFoundException extends ResourceNotFoundException {
    public LoanNotFoundException(String loanId) {
        super("Loan with ID " + loanId + " not found", "APZ000008", "LOAN_NOT_FOUND");
    }
}