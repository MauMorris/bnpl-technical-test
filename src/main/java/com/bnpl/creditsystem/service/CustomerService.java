package com.bnpl.creditsystem.service;

import java.util.UUID;

import com.bnpl.creditsystem.dto.CustomerRequest;
import com.bnpl.creditsystem.dto.CustomerResponse;

public interface CustomerService {
    // Este es el contrato: cualquier clase que se llame CustomerService
    // DEBE tener un método que se llame registerCustomer y otro que se llame findCustomerById
    CustomerResponse registerCustomer(CustomerRequest request);
    CustomerResponse findCustomerById(UUID customerId);
}