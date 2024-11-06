package com.serhat.creditcard.kafka;

public record CardCreatedEvent(
        Integer cardId,
        Status status
) {
}
