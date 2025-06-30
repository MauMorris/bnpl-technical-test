package com.bnpl.creditsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitamos CSRF (no es necesario para esta API).
            .csrf(csrf -> csrf.disable())
            // 2. Regla de autorización: CUALQUIER petición debe estar autenticada.
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            // 3. Habilita explícitamente la autenticación HTTP Basic.
            .httpBasic(withDefaults());

        return http.build();
    }
}