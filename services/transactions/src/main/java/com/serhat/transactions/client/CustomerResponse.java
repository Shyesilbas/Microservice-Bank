package com.serhat.transactions.client;

public record CustomerResponse(
        Integer id,
        String name,
        String surname,
        String email,
        String personalId
) {
}
