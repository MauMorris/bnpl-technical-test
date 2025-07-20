package com.bnpl.creditsystem.service;

import com.bnpl.creditsystem.dto.ClientConsultantResponse;
import com.bnpl.creditsystem.dto.ClientRegistrationRequest;
import com.bnpl.creditsystem.dto.ClientRegistrationResponse;

public interface ClientService {
    // Este es el contrato: cualquier clase que se llame ClientService
    // DEBE tener un método que se llame registerClient y otro que se llame findClientById
    ClientRegistrationResponse registerClient(ClientRegistrationRequest request);
    ClientConsultantResponse findClientById(Long clientId);
}