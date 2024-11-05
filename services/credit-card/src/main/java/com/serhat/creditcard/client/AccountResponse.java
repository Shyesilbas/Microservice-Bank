package com.serhat.creditcard.client;

public record AccountResponse(
        Integer id,
        Integer accountNumber,
        String accountName
) {
}
