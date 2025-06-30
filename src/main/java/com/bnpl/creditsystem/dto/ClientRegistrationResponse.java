package com.bnpl.creditsystem.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegistrationResponse {

    private Long id;
    private BigDecimal assignedCreditLine;

}