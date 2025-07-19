package com.bnpl.creditsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bnpl.creditsystem.dto.PurchaseRequest;
import com.bnpl.creditsystem.dto.PurchaseResponse;
import com.bnpl.creditsystem.entity.Client;
import com.bnpl.creditsystem.entity.Installment;
import com.bnpl.creditsystem.entity.Loan;
import com.bnpl.creditsystem.repository.ClientRepository;
import com.bnpl.creditsystem.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
    private static final Logger log = LoggerFactory.getLogger(PurchaseServiceImpl.class);

    // --- Constantes para las reglas de negocio de compras ---
    private static final List<String> SCHEME_1_INITIALS = List.of("C", "L", "H");
    private static final long SCHEME_2_CLIENT_ID_THRESHOLD = 25L;

    private static final BigDecimal INTEREST_RATE_SCHEME_1 = new BigDecimal("0.13");
    private static final BigDecimal INTEREST_RATE_SCHEME_2 = new BigDecimal("0.16");

    private static final int NUMBER_OF_INSTALLMENTS = 5;
    private static final long DAYS_BETWEEN_INSTALLMENTS = 15L;
    
    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional // ¡MUY IMPORTANTE! Asegura que todas las operaciones de BD se completen o ninguna lo haga.
    public PurchaseResponse processPurchase(PurchaseRequest request) {
        log.info("Processing purchase request for client ID: {} with amount: {}", request.getClientId(), request.getPurchaseAmount());
        
        // 1. Buscar al cliente. Si no existe, lanza una excepción.
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + request.getClientId()));

        // 2. Validar que el monto de la compra no exceda el crédito disponible.
        if (request.getPurchaseAmount().compareTo(client.getAvailableCredit()) > 0) {
            log.warn("Purchase rejected for client ID {}: Insufficient credit. Requested: {}, Available: {}", 
                client.getId(), 
                request.getPurchaseAmount(), 
                client.getAvailableCredit());
                
            throw new IllegalStateException("Insufficient credit line for this purchase.");
        }
        log.info("Found client: {}. Available credit: {}", client.getName(), client.getAvailableCredit());

        // 3. Asignar esquema de pago y tasa de interés según las reglas de negocio.
        BigDecimal interestRate = determineInterestRate(client);

        // 4. Realizar los cálculos internos.
        BigDecimal purchaseAmount = request.getPurchaseAmount();
        BigDecimal commission = purchaseAmount.multiply(interestRate);
        BigDecimal totalAmount = purchaseAmount.add(commission);
        BigDecimal installmentAmount = totalAmount.divide(new BigDecimal(NUMBER_OF_INSTALLMENTS), 2, RoundingMode.HALF_UP);

        // 5. Crear la nueva entidad Loan.
        Loan newLoan = createLoan(request, client);

        // 6. Crear la lista de cuotas (Installments).
        List<Installment> installments = createInstallments(newLoan, installmentAmount);
        newLoan.setInstallments(installments);

        // 7. Actualizar el crédito disponible del cliente.
        BigDecimal newAvailableCredit = client.getAvailableCredit().subtract(purchaseAmount);
        client.setAvailableCredit(newAvailableCredit);
        log.info("Updating client ID {} available credit to: {}", client.getId(), newAvailableCredit);

        // 8. Guardar el nuevo préstamo (y gracias a CascadeType.ALL, sus cuotas también se guardarán).
        // La actualización del cliente también se persistirá gracias a @Transactional.
        Loan savedLoan = loanRepository.save(newLoan);
        log.info("Successfully processed and saved new loan with ID: {}", savedLoan.getId());
        // 9. Devolver la respuesta.
        return new PurchaseResponse(savedLoan.getId());
    }

    /**
     * Determina la tasa de interés aplicable según las reglas de negocio del cliente.
     */
    private BigDecimal determineInterestRate(Client client) {
        String firstNameInitial = client.getName().substring(0, 1).toUpperCase();

        if (SCHEME_1_INITIALS.contains(firstNameInitial)) {
            log.info("Assigning Scheme 1 ({}% interest) to client ID {}", 
            INTEREST_RATE_SCHEME_1.movePointRight(2), 
            client.getId());
            
            return INTEREST_RATE_SCHEME_1;
        } else if (client.getId() > SCHEME_2_CLIENT_ID_THRESHOLD) {
            log.info("Assigning Scheme 2 ({}% interest) to client ID {} based on ID > {}", 
            INTEREST_RATE_SCHEME_2.movePointRight(2), 
            client.getId(), 
            SCHEME_2_CLIENT_ID_THRESHOLD);
            
            return INTEREST_RATE_SCHEME_2;
        } else {
            log.info("Assigning default Scheme 2 ({}% interest) to client ID {}", 
            INTEREST_RATE_SCHEME_2.movePointRight(2), 
            client.getId());
            
            return INTEREST_RATE_SCHEME_2;
        }
    }

    /**
     * Crea y devuelve la lista de cuotas para un préstamo.
     */
    private List<Installment> createInstallments(Loan loan, BigDecimal installmentAmount) {
        List<Installment> installments = new ArrayList<>();
        for (int i = 1; i <= NUMBER_OF_INSTALLMENTS; i++) {
            installments.add(Installment.builder()
                .amount(installmentAmount)
                .dueDate(LocalDate.now().plusDays(DAYS_BETWEEN_INSTALLMENTS * i))
                .loan(loan)
                .build());
        }
        log.info("Created {} installments of {} each for loan.", 
        installments.size(), 
        installmentAmount);
        
        return installments;
    }

    /**
     * Crea una nueva entidad Loan a partir de la solicitud y el cliente.
     */
    private Loan createLoan(PurchaseRequest request, Client client) {
        return new Loan(null, request.getPurchaseAmount(), LocalDateTime.now(), client, null);
    }
}