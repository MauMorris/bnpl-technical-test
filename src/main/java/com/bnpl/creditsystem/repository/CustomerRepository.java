package com.bnpl.creditsystem.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bnpl.creditsystem.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    // Al extender JpaRepository, ya tenemos métodos como:
    // save(), findById(), findAll(), deleteById(), etc.
}