package com.bnpl.creditsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientConsultantResponse {
    private String name;
    private String fatherLastname;
    private String motherLastname;
    private LocalDate birthDate;
    private BigDecimal assignedCreditLine;
    private BigDecimal availableCredit;
}
