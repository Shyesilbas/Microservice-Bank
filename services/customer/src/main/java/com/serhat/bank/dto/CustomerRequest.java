package com.serhat.bank.dto;

import com.serhat.bank.model.Occupation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;


public record CustomerRequest(
        @NotBlank(message = "Personal Id is required")
        String personalId,
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Surname is required")
        String surname,
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,
        @NotNull(message = "Monthly income is required")
        @Positive(message = "Monthly income must be positive")
        BigDecimal monthlyIncome,
        Occupation occupation
) {
}
