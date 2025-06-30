package com.bnpl.creditsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bnpl.creditsystem.dto.ClientRegistrationRequest;
import com.bnpl.creditsystem.dto.ClientRegistrationResponse;
import com.bnpl.creditsystem.entity.Client;
import com.bnpl.creditsystem.repository.ClientRepository;

@ExtendWith(MockitoExtension.class) // Activa la magia de Mockito
class ClientServiceImplTest {

    @Mock // Le dice a Mockito: "Crea una simulación vacía de esta clase"
    private ClientRepository clientRepository;

    @InjectMocks // Le dice a Mockito: "Crea una instancia real de esta clase e inyéctale los @Mock que encuentre"
    private ClientServiceImpl clientService;

    // --- PRUEBAS PARA EL REGISTRO DE CLIENTES ---

    @Test
    @DisplayName("Debe asignar 3000 de crédito a clientes entre 18 y 25 años")
    void shouldAssign3000CreditLine_WhenAgeIsBetween18And25() {
        // Arrange (Arreglar): Preparamos los datos de entrada y el comportamiento de los mocks.
        ClientRegistrationRequest request = new ClientRegistrationRequest();
        request.setName("Juan Perez");
        request.setBirthDate(LocalDate.now().minusYears(20)); // Un cliente de 20 años

        // Creamos una simulación de lo que el repositorio devolverá cuando se guarde el cliente
        Client savedClient = new Client(1L, "Juan Perez", request.getBirthDate(), new BigDecimal("3000"), new BigDecimal("3000"));
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // Act (Actuar): Ejecutamos el método que queremos probar.
        ClientRegistrationResponse response = clientService.registerClient(request);

        // Assert (Afirmar): Verificamos que el resultado es el esperado.
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAssignedCreditLine()).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    @DisplayName("Debe asignar 5000 de crédito a clientes entre 26 y 30 años")
    void shouldAssign5000CreditLine_WhenAgeIsBetween26And30() {
        // Arrange
        ClientRegistrationRequest request = new ClientRegistrationRequest();
        request.setName("Maria Lopez");
        request.setBirthDate(LocalDate.now().minusYears(28)); // Una cliente de 28 años

        Client savedClient = new Client(2L, "Maria Lopez", request.getBirthDate(), new BigDecimal("5000"), new BigDecimal("5000"));
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // Act
        ClientRegistrationResponse response = clientService.registerClient(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAssignedCreditLine()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("Debe lanzar una excepción si el cliente es menor de 18 años")
    void shouldThrowException_WhenClientIsUnderage() {
        // Arrange
        ClientRegistrationRequest request = new ClientRegistrationRequest();
        request.setName("Niño Prodigio");
        request.setBirthDate(LocalDate.now().minusYears(17)); // Un cliente de 17 años

        // Act & Assert
        // Verificamos que al ejecutar el método, se lanza la excepción esperada.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.registerClient(request);
        });

        // Verificamos que el mensaje de la excepción es el correcto.
        assertThat(exception.getMessage()).isEqualTo("Client must be between 18 and 65 years old.");
    }
}