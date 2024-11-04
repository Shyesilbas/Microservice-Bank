package com.serhat.transactions.dto;

import com.serhat.transactions.client.CustomerResponse;

import java.math.BigDecimal;

public record WithdrawResponse(
        String senderAccountNumber,

        BigDecimal amount,
        String description,
        String customerName,
        String customerSurname,
        BigDecimal newBalance


) {
}
