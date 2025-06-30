package com.bnpl.creditsystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bnpl.creditsystem.dto.ClientRegistrationRequest;
import com.bnpl.creditsystem.dto.ClientRegistrationResponse;
import com.bnpl.creditsystem.service.ClientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // Anotación que combina @Controller y @ResponseBody. Prepara la clase para recibir peticiones web y devolver JSON.
@RequestMapping("/api/v1/clients") // Define la URL base para todos los endpoints en esta clase.
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService; // Inyectamos el contrato del servicio, no la implementación.

    @PostMapping // Este método manejará las peticiones HTTP POST a /api/v1/clients
    @ResponseStatus(HttpStatus.CREATED) // Devuelve el código de estado HTTP 201 Created, una buena práctica para la creación de recursos.
    public ClientRegistrationResponse registerClient(@Valid @RequestBody ClientRegistrationRequest request) {
        // La anotación @RequestBody convierte el JSON que nos llega en el cuerpo de la petición a un objeto ClientRegistrationRequest.
        // La anotación @Valid activa las validaciones que pusimos en el DTO (@NotBlank, @Past, etc.).
        
        // El controlador no hace lógica, solo delega al servicio.
        return clientService.registerClient(request);
    }
}