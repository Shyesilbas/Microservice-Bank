package com.serhat.bank.kafka;

public record AccountCreatedEvent(
        Integer customerId,
        int accountNumber,
        Status status


) {
}
