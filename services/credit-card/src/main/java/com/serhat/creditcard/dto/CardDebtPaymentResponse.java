package com.serhat.creditcard.dto;

import java.math.BigDecimal;

public record CardDebtPaymentResponse(
        String customerId,
        String accountNumber,
        String cardNumber,
        String description,
        BigDecimal amount,
        BigDecimal debtLeft,
        BigDecimal balance
) {
}
