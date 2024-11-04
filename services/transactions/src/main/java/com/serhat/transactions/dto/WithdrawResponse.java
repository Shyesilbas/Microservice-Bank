package com.serhat.transactions.dto;

import com.serhat.transactions.client.CustomerResponse;

import java.math.BigDecimal;

public record WithdrawResponse(
        String senderAccountNumber,
        String receiverAccountNumber,
        BigDecimal amount,
        String description,
        CustomerResponse customer


) {
}
