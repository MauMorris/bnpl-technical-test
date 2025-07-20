package com.bnpl.creditsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;

import com.bnpl.creditsystem.dto.ClientConsultantResponse;
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

    // constantes para las reglas de negocio, esto despues se puede guardar en una tabla de BD y consultar
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 65;

    private static final int AGE_TIER_1_UPPER_BOUND = 25;
    private static final int AGE_TIER_2_UPPER_BOUND = 30;
    
    private static final BigDecimal CREDIT_LINE_TIER_1 = new BigDecimal("3000");
    private static final BigDecimal CREDIT_LINE_TIER_2 = new BigDecimal("5000");
    private static final BigDecimal CREDIT_LINE_TIER_3 = new BigDecimal("8000");

    private final ClientRepository clientRepository; // Nuestro acceso a la base de datos de clientes

    @Override // Indicamos que estamos implementando el método del contrato (la interfaz)
    public ClientRegistrationResponse registerClient(ClientRegistrationRequest request) {
        // String que concatena nombre, apellido paterno y materno para utilizar en los logs
        final String fullName = String.format("%s %s %s", request.getName(), request.getFatherLastname(), request.getMotherLastname());
        
        log.info("Attempting to register new client: {}", fullName);

        // 1. Calcular la edad del cliente
        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();
        log.info("Calculated age for client {}: {} years old", fullName, age);

        // 2. Validar la edad según las reglas de negocio
        if (age < MIN_AGE || age > MAX_AGE) {
            log.warn("Client registration rejected for {}: Age {} is out of allowed range ({} - {})", 
            fullName, age, MIN_AGE, MAX_AGE);

            throw new IllegalArgumentException(String.format("Client must be between %d and %d years old.", MIN_AGE, MAX_AGE));
        }

        // 3. Asignar la línea de crédito según la edad
        BigDecimal creditLine;
        if (age <= AGE_TIER_1_UPPER_BOUND) {
            creditLine = CREDIT_LINE_TIER_1;
        } else if (age <= AGE_TIER_2_UPPER_BOUND) {
            creditLine = CREDIT_LINE_TIER_2;
        } else { // AGE_TIER_UPPER_BOUND to MAX_AGE
            creditLine = CREDIT_LINE_TIER_3;
        }
        log.info("Assigned credit line of {} to client {}", creditLine, fullName);

        // 4. Crear la nueva entidad Cliente para guardarla en la BD
        Client newClient = toEntity(request, creditLine);

        // 5. Guardar el nuevo cliente en la base de datos usando el repositorio
        Client savedClient = clientRepository.save(newClient);
        log.info("Successfully saved new client with ID: {}", savedClient.getId());

        // 6. Preparar y devolver la respuesta DTO con los datos del cliente guardado
        return toResponseDto(savedClient);
    }

    /**
     * Convierte un DTO de solicitud y una línea de crédito en una entidad Cliente.
     * El ID se pasa como nulo para indicar que es una nueva entidad.
     * @param request El DTO con los datos de entrada.
     * @param creditLine La línea de crédito calculada.
     * @return Una nueva entidad Client lista para ser guardada.
     */
    private Client toEntity(ClientRegistrationRequest request, BigDecimal creditLine) {
        return new Client(null, request.getName(), request.getFatherLastname(), request.getMotherLastname(),
                request.getBirthDate(), creditLine, creditLine);
    }

    /**
     * Convierte una entidad Cliente en un DTO de respuesta.
     */
    private ClientRegistrationResponse toResponseDto(Client client) {
        return new ClientRegistrationResponse(client.getId(), client.getAssignedCreditLine());
    }

    @Override
    public ClientConsultantResponse findClientById(Long clientId) {
        log.info("Attempting to consult a client by the Id: {}", clientId);

        // 1. Busca el cliente por ID y si no lo encuentra, lanza una excepción.
        Client clientConsulted = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + clientId));

//        final String fullName = String.format("%s %s %s", clientConsulted.getName(), clientConsulted.getFatherLastname(), clientConsulted.getMotherLastname());
        log.info("Successfully retrieving client: {} {} {}", clientConsulted.getName(), clientConsulted.getFatherLastname(), clientConsulted.getMotherLastname());
        // 2. Convierte la entidad a DTO y la devuelve.
        return toConsultantResponseDto(clientConsulted);
    }

    private ClientConsultantResponse toConsultantResponseDto(Client clientConsulted) {
        return new ClientConsultantResponse(
            clientConsulted.getName(), 
            clientConsulted.getFatherLastname(), 
            clientConsulted.getMotherLastname(), 
            clientConsulted.getBirthDate(), 
            clientConsulted.getAssignedCreditLine(), 
            clientConsulted.getAvailableCredit());
    }
}