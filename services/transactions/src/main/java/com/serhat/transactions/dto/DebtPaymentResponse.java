package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record DebtPaymentResponse(
        String customerId,
        String accountNumber,
        String cardNumber,
        BigDecimal amount,
        BigDecimal debtLeft,
        BigDecimal balance
) {
}
