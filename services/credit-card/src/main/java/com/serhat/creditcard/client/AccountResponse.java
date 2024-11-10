package com.serhat.creditcard.client;

import java.math.BigDecimal;

public record AccountResponse(
        Integer id,
        Integer accountNumber,
        String accountName,
        BigDecimal balance
) {
}
