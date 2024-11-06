package com.serhat.expenses.dto;

import java.math.BigDecimal;

public record DebtPaymentRequest(
        String cardNumber,
        String accountNumber,
        BigDecimal amount
) {
}
