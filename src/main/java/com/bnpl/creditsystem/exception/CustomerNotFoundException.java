package com.bnpl.creditsystem.exception;

public class CustomerNotFoundException extends ResourceNotFoundException {
    public CustomerNotFoundException(String customerId) {
        super("Customer with ID " + customerId + " not found", "APZ000005", "CUSTOMER_NOT_FOUND");
    }
}