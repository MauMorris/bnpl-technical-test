package com.bnpl.creditsystem.entity;

/**
 * Statuses:
 * - `ACTIVE`: has pending installment payments.
 * - `LATE`: has installment payments with error.
 * - `COMPLETED`: all installments are paid.
 */
public enum LoanStatus {
    ACTIVE, LATE, COMPLETED
}