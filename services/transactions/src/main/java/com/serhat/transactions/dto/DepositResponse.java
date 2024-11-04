package com.serhat.transactions.dto;

import com.serhat.transactions.client.CustomerResponse;

import java.math.BigDecimal;

public record DepositResponse(
        String receiverAccountNumber,
        String description,
        BigDecimal amount,
        CustomerResponse customer
) {
}
