package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record DebtPaymentRequest(
        Integer customerId,
        String cardNumber,
        String accountNumber,
        BigDecimal amount,
        String description
) {
}
