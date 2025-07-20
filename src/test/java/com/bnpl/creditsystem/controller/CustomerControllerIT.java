package com.bnpl.creditsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bnpl.creditsystem.security.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Levanta el contexto completo de Spring
@AutoConfigureMockMvc // Configura una herramienta para hacer peticiones HTTP falsas
@Testcontainers // Activa la magia de Testcontainers
class CustomerControllerIT {

    @Autowired
    private MockMvc mockMvc; // Herramienta para simular peticiones HTTP a nuestros controladores

    @Autowired
    private JwtService jwtService;

    private String jwtToken;

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

    @BeforeEach
    void setUp() {
        // Creamos un usuario de prueba y generamos un token para él antes de cada test.
        UserDetails testUser = User.builder()
                .username("testuser")
                .password("testpass")
                .roles("USER").build();
        jwtToken = jwtService.generateToken(testUser);
    }

    @Test
    @DisplayName("Prueba de Integración: Debe registrar un cliente y devolver 201 Created")
    void shouldRegisterCustomer_whenRequestIsValid() throws Exception {
        // Arrange
        String requestBody = """
            {
                "firstName": "Cliente",
                "lastName": "De",
                "secondLastName": "Integracion",
                "dateOfBirth": "1990-01-01" 
            }
            """;

        // Act & Assert
        // Simulamos una petición POST a nuestro endpoint con el cuerpo JSON
        mockMvc.perform(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestBody))
                // Verificamos que el status de la respuesta sea 201 Created
                .andExpect(status().isCreated())
                // Verificamos que la respuesta JSON contenga los campos correctos
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.creditLineAmount").value(8000))
                .andExpect(jsonPath("$.availableCreditLineAmount").value(8000))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Prueba de Integración: Debe devolver 400 Bad Request si el cliente es menor de edad")
    void shouldReturnBadRequest_whenCustomerIsUnderage() throws Exception {
        // Arrange
        String requestBody = """
            {
                "firstName": "Menor",
                "lastName": "De",
                "secondLastName": "Edad",
                "dateOfBirth": "2010-01-01"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestBody))
                .andExpect(status().isBadRequest()) // 1. Verifica el código de estado HTTP
                .andExpect(jsonPath("$.code").value("APZ000002")) // 2. Verifica el código de error de la API en el cuerpo JSON
                .andExpect(jsonPath("$.error").value("INVALID_CUSTOMER_REQUEST")) // 3. Verifica el nombre del error
                .andExpect(jsonPath("$.message").value("Customer must be between 18 and 65 years old.")); // 4. Verifica el mensaje específico
    }

    @Test
    @DisplayName("Prueba de Integración: Debe devolver 400 Bad Request si el nombre está en blanco")
    void shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        // Arrange
        String requestBody = """
            {
                "firstName": "",
                "lastName": "Sin",
                "secondLastName": "Nombre",
                "dateOfBirth": "1990-01-01"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(requestBody))
                .andExpect(status().isBadRequest()) // Verifica el código de estado HTTP
                .andExpect(jsonPath("$.code").value("APZ000002")) // Verifica el código de error de la API
                .andExpect(jsonPath("$.message").value("'firstName': Name cannot be blank")); // Verifica el mensaje de validación
    }
}