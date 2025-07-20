package com.bnpl.creditsystem.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank(message = "Name cannot be blank")
    private String firstName;

    @NotBlank(message = "Father Lastname cannot be blank")
    private String lastName;

    @NotBlank(message = "mother lastname cannot be blank")
    private String secondLastName;

    @NotNull(message = "Birth date cannot be null")
    @Past(message = "Birth date must be in the past")
    private LocalDate dateOfBirth;
}