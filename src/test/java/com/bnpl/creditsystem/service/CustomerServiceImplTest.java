package com.bnpl.creditsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bnpl.creditsystem.dto.CustomerRequest;
import com.bnpl.creditsystem.dto.CustomerResponse;
import com.bnpl.creditsystem.entity.Customer;
import com.bnpl.creditsystem.exception.InvalidAgeException;
import com.bnpl.creditsystem.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class) // Activa la magia de Mockito
class CustomerServiceImplTest {

    @Mock // Le dice a Mockito: "Crea una simulación vacía de esta clase"
    private CustomerRepository customerRepository;

    @InjectMocks // Le dice a Mockito: "Crea una instancia real de esta clase e inyéctale los @Mock que encuentre"
    private CustomerServiceImpl customerService;

    // --- PRUEBAS PARA EL REGISTRO DE CUSTOMERS ---

    @Test
    @DisplayName("Debe asignar 3000 de crédito a customers entre 18 y 25 años")
    void shouldAssign3000CreditLine_WhenAgeIsBetween18And25() {
        // Arrange (Arreglar): Preparamos los datos de entrada y el comportamiento de los mocks.
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Juan Perez");
        request.setDateOfBirth(LocalDate.now().minusYears(20)); // Un customer de 20 años

        UUID customerId = UUID.randomUUID();

        // Creamos una simulación de lo que el repositorio devolverá cuando se guarde el customer
        Customer savedCustomer = new Customer(
            customerId,
            "Juan",
            "Perez",
            "Perez",
            request.getDateOfBirth(),
            new BigDecimal("3000"),
            new BigDecimal("3000"),
            LocalDateTime.now()
            );

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // Act (Actuar): Ejecutamos el método que queremos probar.
        CustomerResponse response = customerService.registerCustomer(request);

        // Assert (Afirmar): Verificamos que el resultado es el esperado.
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(customerId);
        assertThat(response.getCreditLineAmount()).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    @DisplayName("Debe asignar 5000 de crédito a clientes entre 26 y 30 años")
    void shouldAssign5000CreditLine_WhenAgeIsBetween26And30() {
        // Arrange
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Maria Lopez");
        request.setDateOfBirth(LocalDate.now().minusYears(28)); // Una customer de 28 años

        UUID customerId = UUID.randomUUID();

        Customer savedCustomer = new Customer(
            customerId, 
            "Maria", 
            "Lopez", 
            "Lopez", 
            request.getDateOfBirth(), 
            new BigDecimal("5000"), 
            new BigDecimal("5000"),
            LocalDateTime.now()
            );
            
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // Act
        CustomerResponse response = customerService.registerCustomer(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCreditLineAmount()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("Debe lanzar una excepción si el customer es menor de 18 años")
    void shouldThrowException_WhenCustomerIsUnderage() {
        // Arrange
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Niño Prodigio");
        request.setDateOfBirth(LocalDate.now().minusYears(17)); // Un customer de 17 años

        // Act & Assert
        // Verificamos que al ejecutar el método, se lanza la excepción esperada.
        InvalidAgeException exception = assertThrows(InvalidAgeException.class, () -> {
            customerService.registerCustomer(request);
        });

        // Verificamos que el mensaje de la excepción es el correcto.
        assertThat(exception.getMessage()).isEqualTo("Customer must be between 18 and 65 years old.");
    }
}