package com.bnpl.creditsystem.service;

import com.bnpl.creditsystem.dto.ClientRegistrationRequest;
import com.bnpl.creditsystem.dto.ClientRegistrationResponse;

public interface ClientService {
    // Este es el contrato: cualquier clase que se llame ClientService
    // DEBE tener un método que se llame registerClient.
    ClientRegistrationResponse registerClient(ClientRegistrationRequest request);
}