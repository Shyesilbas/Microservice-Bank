package com.serhat.expenses.dto;

public record CustomerResponse(
        String id,
        String name,
        String surname,
        String email,
        String personalId
) {
}
