package com.serhat.bank.client;

import com.serhat.bank.dto.CustomerResponse;

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
}
