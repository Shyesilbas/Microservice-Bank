package com.serhat.creditcard.kafka;

import java.math.BigDecimal;

public record PayedCardDebtEvent(
        String customerId,
        String accountNumber,
        String cardNumber,
        String description,
        BigDecimal amount,
        BigDecimal debtLeft,
        BigDecimal balance
) {
}
