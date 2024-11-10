package com.serhat.creditcard.dto;

import java.math.BigDecimal;

public record CardDebtPaymentRequest(
        Integer customerId,
        String cardNumber,
        String description,
        String accountNumber,
        BigDecimal amount
) {
}
