package com.bnpl.creditsystem.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Levanta el contexto completo de Spring
@AutoConfigureMockMvc // Configura una herramienta para hacer peticiones HTTP falsas
@Testcontainers // Activa la magia de Testcontainers
class ClientControllerIT {

    @Autowired
    private MockMvc mockMvc; // Herramienta para simular peticiones HTTP a nuestros controladores

    @Container // Le dice a Testcontainers que gestione este contenedor
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    // Este método es crucial: intercepta la configuración de Spring y la sobreescribe
    // con la URL, usuario y contraseña de la base de datos REAL que Testcontainers acaba de crear.
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Test
    @DisplayName("Prueba de Integración: Debe registrar un cliente y devolver 201 Created")
    void shouldRegisterClient_whenRequestIsValid() throws Exception {
        // Arrange
        String requestBody = """
            {
                "name": "Cliente de Integracion",
                "birthDate": "1990-01-01"
            }
            """;

        // Act & Assert
        // Simulamos una petición POST a nuestro endpoint con el cuerpo JSON
        mockMvc.perform(post("/api/v1/clients")
                .with(httpBasic("testuser", "testpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // Verificamos que el status de la respuesta sea 201 Created
                .andExpect(status().isCreated())
                // Verificamos que la respuesta JSON contenga los campos correctos
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.assignedCreditLine").value(8000));
    }
}