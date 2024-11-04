package com.serhat.bank.client;

import java.math.BigDecimal;

public record DepositRequest(
        String customerId,
        String accountNumber,
        BigDecimal amount,
        String description
) {
}
