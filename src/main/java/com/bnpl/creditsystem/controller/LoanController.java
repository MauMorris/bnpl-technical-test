package com.bnpl.creditsystem.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bnpl.creditsystem.dto.LoanRequest;
import com.bnpl.creditsystem.dto.LoanResponse;

import com.bnpl.creditsystem.service.LoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // @Controller + @ResponseBody. Prepara para recibir peticiones web y devolver JSON.
@RequestMapping("/v1/loans") // Define la URL base para todos los endpoints en esta clase.
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService; // Inyectamos el contrato del servicio.

    /**
     * @RequestBody convierte el JSON que nos llega en el cuerpo de la petición a un objeto LoanRequest.
     * @Valid activa las validaciones que pusimos en el DTO (@NotBlank, @Past, etc.).
     * @PostMapping manejará las peticiones HTTP POST a /v1/loan.
     * Al igual que antes, el controlador solo delega el trabajo al servicio.
     * Devuelve el código de estado HTTP 201 Created y en el body el resultado del servicio.
    */
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest request) {
        LoanResponse loanResponse = loanService.createLoan(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(loanResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(loanResponse);
    }

    /**
     * @GetMapping Maneja las peticiones HTTP GET a v1/loan/{loanid}
     * @param loanId toma el ID del prestamo para consultar su información
     * @return regresa el objeto loan incluido su estatus
     */
    @GetMapping({"/{loanId}"})
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable("loanId") UUID loanId){
        LoanResponse loanResponse = loanService.findLoanById(loanId);
        return ResponseEntity.ok(loanResponse);
    }
}