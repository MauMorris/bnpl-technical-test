package com.bnpl.creditsystem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bnpl.creditsystem.dto.CustomerRequest;
import com.bnpl.creditsystem.dto.CustomerResponse;
import com.bnpl.creditsystem.entity.Customer;
import com.bnpl.creditsystem.exception.InvalidAgeException;
import com.bnpl.creditsystem.exception.CustomerNotFoundException;
import com.bnpl.creditsystem.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service // Le dice a Spring que esta clase contiene lógica de negocio y la gestiona.
@RequiredArgsConstructor // Crea un constructor con los campos 'final' (inyección de dependencias)
public class CustomerServiceImpl implements CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    // constantes para las reglas de negocio, esto despues se puede guardar en una tabla de BD y consultar
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 65;

    private static final int AGE_TIER_1_UPPER_BOUND = 25;
    private static final int AGE_TIER_2_UPPER_BOUND = 30;
    
    private static final BigDecimal CREDIT_LINE_TIER_1 = new BigDecimal("3000");
    private static final BigDecimal CREDIT_LINE_TIER_2 = new BigDecimal("5000");
    private static final BigDecimal CREDIT_LINE_TIER_3 = new BigDecimal("8000");

    private final CustomerRepository customerRepository; // Nuestro acceso a la base de datos de customers

    @Override // Indicamos que estamos implementando el método del contrato (la interfaz)
    public CustomerResponse registerCustomer(CustomerRequest request) {
        // String que concatena nombre, apellido paterno y materno para utilizar en los logs
        final String fullName = String.format("%s %s %s", request.getFirstName(), request.getLastName(), request.getSecondLastName());
        
        log.info("Attempting to register new customer: {}", fullName);

        // 1. Calcular la edad del customer
        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
        log.info("Calculated age for customer {}: {} years old", fullName, age);

        // 2. Validar la edad según las reglas de negocio
        if (age < MIN_AGE || age > MAX_AGE) {
            log.warn("Customer registration rejected for {}: Age {} is out of allowed range ({} - {})",
            fullName, age, MIN_AGE, MAX_AGE);

            throw new InvalidAgeException(String.format("Customer must be between %d and %d years old.", MIN_AGE, MAX_AGE));
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
        log.info("Assigned credit line of {} to customer {}", creditLine, fullName);

        // 4. Crear la nueva entidad customer para guardarla en la BD
        Customer newCustomer = toEntity(request, creditLine);

        // 5. Guardar el nuevo customer en la base de datos usando el repositorio
        Customer savedCustomer = customerRepository.save(newCustomer);
        log.info("Successfully saved new customer with ID: {}", savedCustomer.getId());

        // 6. Preparar y devolver la respuesta DTO con los datos del customere guardado
        return toCustomerResponse(savedCustomer);
    }

    /**
     * Convierte un DTO de solicitud y una línea de crédito en una entidad customer.
     * El ID se pasa como nulo para indicar que es una nueva entidad.
     * @param request El DTO con los datos de entrada.
     * @param creditLine La línea de crédito calculada.
     * @return Una nueva entidad Customer lista para ser guardada.
     */
    private Customer toEntity(CustomerRequest request, BigDecimal creditLine) {
        return new Customer(null, request.getFirstName(), request.getLastName(), request.getSecondLastName(),
                request.getDateOfBirth(), creditLine, creditLine, LocalDateTime.now());
    }

    /**
     * Convierte una entidad customere en un DTO de respuesta.
     */
    private CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getAssignedCreditLine(), customer.getAvailableCredit(), customer.getCreatedAt());
    }

    @Override
    public CustomerResponse findCustomerById(UUID customerId) {
        log.info("Attempting to consult a customer by the Id: {}", customerId);

        // 1. Busca el customer por ID y si no lo encuentra, lanza una excepción.
        Customer customerConsulted = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(customerId)));

        log.info("Successfully retrieving customer: {} {} {}", customerConsulted.getFirstName(), customerConsulted.getLastName(), customerConsulted.getSecondLastName());
        // 2. Convierte la entidad a DTO y la devuelve.
        return toCustomerResponse(customerConsulted);
    }
}