package com.serhat.transactions.client;

import com.serhat.transactions.entity.Status;
import com.serhat.transactions.entity.TransactionType;

import java.math.BigDecimal;

public record UpdateResponse(
        String customerId,
        String accountNumber,
        BigDecimal amount,
        TransactionType transactionType,
        BigDecimal updatedBalance
) {
}
