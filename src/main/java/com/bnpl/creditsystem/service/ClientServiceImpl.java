package com.bnpl.creditsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;

import com.bnpl.creditsystem.dto.ClientRegistrationRequest;
import com.bnpl.creditsystem.dto.ClientRegistrationResponse;
import com.bnpl.creditsystem.entity.Client;
import com.bnpl.creditsystem.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service // Le dice a Spring que esta clase contiene lógica de negocio y la gestiona.
@RequiredArgsConstructor // Crea un constructor con los campos 'final' (inyección de dependencias)
public class ClientServiceImpl implements ClientService {
    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository; // Nuestro acceso a la base de datos de clientes

    @Override // Indicamos que estamos implementando el método del contrato (la interfaz)
    public ClientRegistrationResponse registerClient(ClientRegistrationRequest request) {
        log.info("Attempting to register new client: {}", request.getName());

        // 1. Calcular la edad del cliente
        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();
        log.info("Calculated age for client {}: {} years", request.getName(), age);

        // 2. Validar la edad según las reglas de negocio
        if (age < 18 || age > 65) {
            log.warn("Client registration rejected for {}: Age {} is out of allowed range (18-65)", request.getName(), age);
            throw new IllegalArgumentException("Client must be between 18 and 65 years old.");
        }

        // 3. Asignar la línea de crédito según la edad
        BigDecimal creditLine;
        if (age >= 18 && age <= 25) {
            creditLine = new BigDecimal("3000");
        } else if (age >= 26 && age <= 30) {
            creditLine = new BigDecimal("5000");
        } else { // 31 a 65
            creditLine = new BigDecimal("8000");
        }
        log.info("Assigned credit line of {} to client {}", creditLine, request.getName());

        // 4. Crear la nueva entidad Cliente para guardarla en la BD
        Client newClient = new Client();
        newClient.setName(request.getName());
        newClient.setBirthDate(request.getBirthDate());
        newClient.setAssignedCreditLine(creditLine);
        newClient.setAvailableCredit(creditLine); // Al inicio, el crédito disponible es igual al asignado

        // 5. Guardar el nuevo cliente en la base de datos usando el repositorio
        Client savedClient = clientRepository.save(newClient);
        log.info("Successfully saved new client with ID: {}", savedClient.getId());

        // 6. Preparar y devolver la respuesta DTO con los datos del cliente guardado
        return new ClientRegistrationResponse(savedClient.getId(), savedClient.getAssignedCreditLine());
    }
}