package com.serhat.transactions.client;

import java.math.BigDecimal;

public record AccountResponse(
        Integer id,
        Integer accountNumber,
        String accountName,
        Currency currency,
        AccountType accountType,
        BigDecimal balance,
        CustomerResponse customer


) {
    public void setBalance(BigDecimal add) {
    }
}
