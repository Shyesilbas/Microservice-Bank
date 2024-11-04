package com.serhat.bank.kafka;

public record CustomerCreatedEvent(
        String customerId,
        Status status

) {
}
