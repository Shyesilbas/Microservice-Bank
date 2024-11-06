package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record CreditCardResponse(
        String customerId,
        String customerName,
        String customerSurname,
        String cardNumber,
        BigDecimal balance,
        BigDecimal debt
) {
}
