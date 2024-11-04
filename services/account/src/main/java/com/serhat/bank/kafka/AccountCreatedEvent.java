package com.serhat.bank.kafka;

public record AccountCreatedEvent(
        int accountNumber,
        Status status


) {
}
