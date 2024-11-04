package com.serhat.bank.client;

import java.math.BigDecimal;

public record WithdrawResponse(
        int senderAccountNumber,
        BigDecimal amount,
        String description,
        CustomerResponse customer
) {
}
