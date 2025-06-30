package com.bnpl.creditsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bnpl.creditsystem.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    // Al extender JpaRepository, ya tenemos métodos como:
    // save(), findById(), findAll(), deleteById(), etc.
}