package com.serhat.bank.dto;

import com.serhat.bank.client.CustomerResponse;
import com.serhat.bank.model.AccountType;
import com.serhat.bank.model.Currency;

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
