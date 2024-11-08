package com.serhat.loan.dto;

public record CustomerResponse(
        Integer id,
        String name,
        String surname,
        String email,
        String personalId
) {
}
