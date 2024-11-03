package com.serhat.bank.dto;

import com.serhat.bank.model.AccountType;
import com.serhat.bank.model.Currency;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(
        String accountName,
        Currency currency,
        AccountType accountType,
        BigDecimal balance,
        Integer customerId


) {
}
