package com.serhat.expenses.dto;

import java.math.BigDecimal;

public record CreditCardResponse(
        String customerName,
        String customerSurname,
        String cardNumber,
        BigDecimal balance,
        BigDecimal debt
) {
}
