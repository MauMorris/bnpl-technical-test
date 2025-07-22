package com.bnpl.creditsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bnpl.creditsystem.dto.LoanRequest;
import com.bnpl.creditsystem.dto.LoanResponse;

import com.bnpl.creditsystem.entity.Customer;
import com.bnpl.creditsystem.entity.Installment;
import com.bnpl.creditsystem.entity.InstallmentStatus;
import com.bnpl.creditsystem.entity.Loan;
import com.bnpl.creditsystem.entity.LoanStatus;
import com.bnpl.creditsystem.exception.CustomerNotFoundException;
import com.bnpl.creditsystem.exception.InsufficientCreditException;
import com.bnpl.creditsystem.exception.LoanNotFoundException;
import com.bnpl.creditsystem.mapper.LoanMapper;
import com.bnpl.creditsystem.repository.CustomerRepository;
import com.bnpl.creditsystem.repository.LoanRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
    private static final Logger log = LoggerFactory.getLogger(LoanServiceImpl.class);

    // --- Constantes para las reglas de negocio de compras ---
    private static final List<String> SCHEME_1_INITIALS = List.of("C", "L", "H");

    private static final BigDecimal INTEREST_RATE_SCHEME_1 = new BigDecimal("0.13");
    private static final BigDecimal INTEREST_RATE_SCHEME_2 = new BigDecimal("0.16");

    private static final int NUMBER_OF_INSTALLMENTS = 5;
    private static final long DAYS_BETWEEN_INSTALLMENTS = 15L;
    
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    
    private final LoanMapper loanMapper;

    @Override
    @Transactional // ¡MUY IMPORTANTE! Asegura que todas las operaciones de BD se completen o ninguna lo haga.
    public LoanResponse createLoan(LoanRequest request) {
        log.info("Processing loan request for customer ID: {} with amount: {}", request.getCustomerId(), request.getAmount());
        
        // 1. Buscar al customer por ID y si no existe, lanza una excepción.
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(String.valueOf(request.getCustomerId())));

        // 2. Validar que el monto de la compra no exceda el crédito disponible.
        if (request.getAmount().compareTo(customer.getAvailableCreditLineAmount()) > 0) {
            log.warn("Purchase rejected for customer ID {}: Insufficient credit. Requested: {}, Available: {}", 
                customer.getId(), 
                request.getAmount(), 
                customer.getAvailableCreditLineAmount());
                
            throw new InsufficientCreditException("Insufficient credit line for this loan.");
        }
        log.info("Found customer: {}. Available credit: {}", customer.getFirstName(), customer.getAvailableCreditLineAmount());

        // 3. Asignar esquema de pago y tasa de interés según las reglas de negocio.
        BigDecimal interestRate = determineInterestRate(customer);

        // 4. Realizar los cálculos internos.
        BigDecimal loanAmount = request.getAmount();
        BigDecimal commission = loanAmount.multiply(interestRate);
        BigDecimal totalAmount = loanAmount.add(commission);
        BigDecimal installmentAmount = totalAmount.divide(new BigDecimal(NUMBER_OF_INSTALLMENTS), 2, RoundingMode.HALF_UP);

        // 5. Crear la nueva entidad Loan.
        Loan newLoan = buildLoanEntity(request, interestRate, commission, totalAmount, customer);

        // 6. Crear la lista de cuotas (Installments).
        List<Installment> installments = createInstallments(newLoan, installmentAmount);
        newLoan.setInstallments(installments);

        // 7. Actualizar el crédito disponible del customer.
        BigDecimal newAvailableCredit = customer.getAvailableCreditLineAmount().subtract(loanAmount);
        customer.setAvailableCreditLineAmount(newAvailableCredit);
        log.info("Updating customer ID {} available credit to: {}", customer.getId(), newAvailableCredit);

        // 8. Guardar el nuevo préstamo (y gracias a CascadeType.ALL, sus cuotas también se guardarán).
        // La actualización del customer también se persistirá gracias a @Transactional.
        Loan savedLoan = loanRepository.save(newLoan);
        log.info("Successfully processed and saved new loan with ID: {}", savedLoan.getId());
        // 9. Devolver la respuesta.
        return loanMapper.toLoanResponse(savedLoan);
    }

    /**
     * Determina la tasa de interés aplicable según las reglas de negocio del customer.
     */
    private BigDecimal determineInterestRate(Customer customer) {
        String firstNameInitial = customer.getFirstName().substring(0, 1).toUpperCase();

        if (SCHEME_1_INITIALS.contains(firstNameInitial)) {
            log.info("Assigning Scheme 1 ({}% interest) to customer ID {}", 
            INTEREST_RATE_SCHEME_1.movePointRight(2), 
            customer.getId());
            
            return INTEREST_RATE_SCHEME_1;
        } else {
            log.info("Assigning default Scheme 2 ({}% interest) to customer ID {}", 
            INTEREST_RATE_SCHEME_2.movePointRight(2), 
            customer.getId());
            
            return INTEREST_RATE_SCHEME_2;
        }
    }

    /**
     * Crea una nueva entidad Loan a partir de la solicitud y el customer.
     * @param installmentAmount 
     * @param totalAmount 
     * @param commission 
     * @param interestRate 
     */
    private Loan buildLoanEntity(LoanRequest request, BigDecimal interestRate, BigDecimal commission, BigDecimal totalAmount, Customer customer) {
        return new Loan(null, request.getAmount(), LocalDateTime.now(), LoanStatus.ACTIVE, interestRate, commission, totalAmount, customer, null);
    }

    /**
     * Crea y devuelve la lista de cuotas para un préstamo.
     */
    private List<Installment> createInstallments(Loan loan, BigDecimal installmentAmount) {
        List<Installment> installments = new ArrayList<>();
        for (int i = 1; i <= NUMBER_OF_INSTALLMENTS; i++) {
            installments.add(Installment.builder()
                .amount(installmentAmount)
                .status(InstallmentStatus.PENDING)
                .scheduledPaymentDate(LocalDate.now().plusDays(DAYS_BETWEEN_INSTALLMENTS * i))
                .loan(loan)
                .build());
        }
        log.info("Created {} installments of {} each for loan.", 
        installments.size(), 
        installmentAmount);
        
        return installments;
    }

    @Override
    public LoanResponse findLoanById(UUID loanId) {
        log.info("Attempting to consult a loan by the Id: {}", loanId);
        
        // 1. Busca el loan por ID y si no lo encuentra, lanza una excepción.
        Loan loanConsulted = loanRepository.findById(loanId).
        orElseThrow(() -> new LoanNotFoundException(String.valueOf(loanId)));
        log.info("Successfully retrieving loan: {}", loanConsulted.getId());
        
        // 2. Convierte la entidad a DTO y la devuelve.
        return loanMapper.toLoanResponse(loanConsulted);
    }
}