package com.bnpl.creditsystem.entity;

/**
 * Statuses:
 * - `NEXT`: marked as next installment to be paid
 * - `PENDING`: installment still pending to be paid
 * - `ERROR`: error on payment, considered unpaid
 * - `PAID`: payed installment
 */
public enum InstallmentStatus {
    NEXT, PENDING, ERROR
}