package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record DepositResponse(
        String receiverAccountNumber,
        String description,
        BigDecimal amount,
        String customerId,
        BigDecimal updatedBalance
) {
}
