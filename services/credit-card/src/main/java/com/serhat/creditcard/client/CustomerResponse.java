package com.serhat.creditcard.client;

public record CustomerResponse(
        String id,
        String name,
        String surname,
        String email,
        String personalId

) {
}
