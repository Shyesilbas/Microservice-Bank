package com.serhat.expenses.kafka;

import java.math.BigDecimal;

public record PaymentSuccessfulEvent(
        String cardNumber,
        Integer paymentId,
        BigDecimal amount,
        Status status
) {
}
