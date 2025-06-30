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
                client.getId(), request.getPurchaseAmount(), client.getAvailableCredit());
            throw new IllegalStateException("Insufficient credit line for this purchase.");
        }
        log.info("Found client: {}. Available credit: {}", client.getName(), client.getAvailableCredit());

        // 3. Asignar esquema de pago y tasa de interés según las reglas de negocio.
        // Reglas: Scheme 1 si el primer nombre empieza con C, L, o H. Scheme 2 si id > 25. Default: Scheme 2.
        BigDecimal interestRate;
        String firstNameInitial = client.getName().substring(0, 1).toUpperCase();

        if (List.of("C", "L", "H").contains(firstNameInitial)) {
            log.info("Assigning Scheme 1 (13% interest) to client ID {}", client.getId());
            interestRate = new BigDecimal("0.13"); // Scheme 1: 13%
        } else if (client.getId() > 25) {
            log.info("Assigning Scheme 2 (16% interest) to client ID {} based on ID > 25", client.getId());
            interestRate = new BigDecimal("0.16"); // Scheme 2: 16%
        } else {
            log.info("Assigning default Scheme 2 (16% interest) to client ID {}", client.getId());
            interestRate = new BigDecimal("0.16"); // Default: Scheme 2
        }

        // 4. Realizar los cálculos internos.
        BigDecimal purchaseAmount = request.getPurchaseAmount();
        BigDecimal commission = purchaseAmount.multiply(interestRate);
        BigDecimal totalAmount = purchaseAmount.add(commission);
        BigDecimal installmentAmount = totalAmount.divide(new BigDecimal(5), 2, RoundingMode.HALF_UP);

        // 5. Crear la nueva entidad Loan.
        Loan newLoan = new Loan();
        newLoan.setPurchaseAmount(purchaseAmount);
        newLoan.setPurchaseDate(LocalDateTime.now());
        newLoan.setClient(client);

        // 6. Crear la lista de cuotas (Installments).
        List<Installment> installments = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            installments.add(Installment.builder()
                .amount(installmentAmount)
                .dueDate(LocalDate.now().plusDays(15L * i)) // Biweekly payments
                .loan(newLoan) // Asocia esta cuota con el préstamo
                .build());
        }
        log.info("Created {} installments of {} each for loan.", installments.size(), installmentAmount);
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
}