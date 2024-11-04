package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record DepositRequest(
        String customerId,
        String accountNumber,
        BigDecimal amount,
        String description



) {
}
