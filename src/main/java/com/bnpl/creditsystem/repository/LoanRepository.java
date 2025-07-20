package com.bnpl.creditsystem.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bnpl.creditsystem.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    // Al igual que antes, Spring nos da todos los métodos básicos.
}