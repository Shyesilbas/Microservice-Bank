package com.serhat.bank.client;

import java.math.BigDecimal;

public record DepositResponse(
        int receiverAccountNumber,
        String description,
        BigDecimal amount,
        CustomerResponse customer
) {
}
