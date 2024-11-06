package com.serhat.expenses.kafka;

public record PaymentSuccessfulEvent(
        Integer paymentId,
        Status status
) {
}
