package com.serhat.expenses.dto;

import java.math.BigDecimal;

public record DebtPaymentResponse(

        String accountNumber,
        String cardNumber,
        BigDecimal amount,
        BigDecimal debtLeft,
        BigDecimal balance
) {
}
