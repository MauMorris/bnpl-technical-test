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

import com.bnpl.creditsystem.dto.CustomerRequest;
import com.bnpl.creditsystem.dto.CustomerResponse;

import com.bnpl.creditsystem.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // Anotación @Controller + @ResponseBody. Prepara para recibir peticiones web y devolver JSON.
@RequestMapping("/v1/customers") // Define la URL base para todos los endpoints en esta clase.
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService; // Inyectamos el contrato del servicio, no la implementación.

    /**
     * @RequestBody convierte el JSON que nos llega en el cuerpo de la petición a un objeto CustomerRequest.
     * @Valid activa las validaciones que pusimos en el DTO (@NotBlank, @Past, etc.).
     * @PostMapping manejará las peticiones HTTP POST a /v1/clients.
     * El controlador solo delega el trabajo al servicio.
     * Devuelve el código de estado HTTP 201 Created y en el body el resultado del servicio.
    */
    @PostMapping
    public ResponseEntity <CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
        CustomerResponse customerResponse = customerService.registerCustomer(customerRequest);

    // Construimos la URI del recurso creado para devolverla en el header "Location".
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(customerResponse.getId())
                .toUri();        
        return ResponseEntity.created(location).body(customerResponse);
    }

    @GetMapping({"/{customerId}"})
    public ResponseEntity<CustomerResponse> getClientById(@PathVariable("customerId") UUID customerId) {
        CustomerResponse clientResponse = customerService.findCustomerById(customerId);
        return ResponseEntity.ok(clientResponse);
    }
}