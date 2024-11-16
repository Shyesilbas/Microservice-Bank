package com.serhat.bank.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.serhat.bank.client.CustomerResponse;
import com.serhat.bank.model.AccountType;
import com.serhat.bank.model.Currency;

import java.io.Serializable;
import java.math.BigDecimal;


public record AccountResponse(
        Integer id,
        Integer accountNumber,
        String accountName,
        Currency currency,
        AccountType accountType,
        BigDecimal balance,
        CustomerResponse customer
) implements Serializable {
    private static final long serialVersionUID = 1L;

}
