package com.bnpl.creditsystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnpl.creditsystem.dto.PurchaseRequest;
import com.bnpl.creditsystem.dto.PurchaseResponse;
import com.bnpl.creditsystem.service.PurchaseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // Anotación @Controller + @ResponseBody. Prepara para recibir peticiones web y devolver JSON.
@RequestMapping("/v1/purchases") // Define la URL base para todos los endpoints en esta clase.
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService; // Inyectamos el contrato del servicio.

    @PostMapping // Este método manejará las peticiones HTTP POST a /v1/purchases
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        // @RequestBody convierte el JSON que nos llega en el cuerpo de la petición a un objeto PurchaseRequest.
        // @Valid activa las validaciones que pusimos en el DTO (@NotBlank, @Past, etc.).
        PurchaseResponse purchaseResponse = purchaseService.processPurchase(request);
        // Al igual que antes, el controlador solo delega el trabajo al servicio.
        // Devuelve el código de estado HTTP 201 Created y en el body el resultado del servicio.
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseResponse);
    }
}