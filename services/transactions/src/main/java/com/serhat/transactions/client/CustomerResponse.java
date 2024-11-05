package com.serhat.transactions.client;

public record CustomerResponse(
        String id,
        String name,
        String surname,
        String email,
        String personalId
) {
}
