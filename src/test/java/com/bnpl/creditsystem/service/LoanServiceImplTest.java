package com.bnpl.creditsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bnpl.creditsystem.dto.LoanRequest;
import com.bnpl.creditsystem.dto.LoanResponse;
import com.bnpl.creditsystem.entity.Customer;
import com.bnpl.creditsystem.entity.Loan;
import com.bnpl.creditsystem.exception.InsufficientCreditException;
import com.bnpl.creditsystem.repository.CustomerRepository;
import static org.mockito.Mockito.verify;
import com.bnpl.creditsystem.mapper.LoanMapper;
import com.bnpl.creditsystem.repository.LoanRepository;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Captor
    private ArgumentCaptor<Loan> loanArgumentCaptor;

    @Test
    @DisplayName("Debe procesar un préstamo válido correctamente")
    void shouldProcessLoan_WhenRequestIsValid() {
        // Arrange
        // 1. Preparamos los datos de entrada
        UUID customerId = UUID.randomUUID();
        LoanRequest request = new LoanRequest();
        request.setCustomerId(customerId);
        request.setAmount(new BigDecimal("1000"));

        // 2. Simulamos que el cliente existe en la BD y tiene crédito suficiente
        Customer mockCustomer = new Customer(
            customerId,
            "Carlos",
            "Valdez",
            "Valdez",
            LocalDate.now().minusYears(35),
            new BigDecimal("8000"),
            new BigDecimal("8000"),
            LocalDateTime.now()
            );

        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockCustomer));

        // 3. Simulamos la respuesta del repositorio al guardar el préstamo
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loanToSave = invocation.getArgument(0);
            loanToSave.setId(UUID.randomUUID()); // Le asignamos un ID al préstamo guardado
            return loanToSave;
        });

        // 4. Simulamos la respuesta del mapper, que ahora es una dependencia del servicio.
        when(loanMapper.toLoanResponse(any(Loan.class))).thenAnswer(invocation -> {
            Loan savedLoan = invocation.getArgument(0);
            // Para el propósito de este test, solo necesitamos que el mapper devuelva un objeto
            // con los IDs correctos para que las aserciones pasen.
            return new LoanResponse(savedLoan.getId(), savedLoan.getCustomer().getId(), null, null, null, null);
        });

        // Act
        // 5. Ejecutamos el método a probar
        LoanResponse response = loanService.createLoan(request);

        // Assert
        // 6. Verificamos que la respuesta es la correcta
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo(customerId);

        // Capturamos el objeto Loan que se pasó al método save()
        verify(loanRepository).save(loanArgumentCaptor.capture());
        Loan capturedLoan = loanArgumentCaptor.getValue();

        // Verificamos que el crédito del cliente asociado al préstamo guardado se descontó correctamente
        assertThat(capturedLoan.getCustomer().getAvailableCreditLineAmount()).isEqualByComparingTo(new BigDecimal("7000"));
    }

    @Test
    @DisplayName("Debe lanzar una excepción si el crédito es insuficiente")
    void shouldThrowException_WhenCreditIsInsufficient() {
        // Arrange
        LoanRequest request = new LoanRequest();
        UUID customerId = UUID.randomUUID();
        request.setCustomerId(customerId);
        request.setAmount(new BigDecimal("9000")); // Un monto que excede el crédito

        // Simulamos un cliente que solo tiene 8000 de crédito disponible
        Customer mockCustomer = new Customer(
            customerId, 
            "Customer",
            "Apellido",
            "Apellido",
            LocalDate.now().minusYears(40),
            new BigDecimal("8000"),
            new BigDecimal("8000"),
            LocalDateTime.now()
            );

        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockCustomer));

        // Act & Assert
        // Verificamos que se lanza la excepción correcta con el mensaje correcto
        InsufficientCreditException exception = assertThrows(InsufficientCreditException.class, () -> {
            loanService.createLoan(request);
        });

        assertThat(exception.getMessage()).isEqualTo("Insufficient credit line for this loan.");
    }
}