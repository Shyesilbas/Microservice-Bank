package com.serhat.transactions.dto;

import com.serhat.transactions.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistory(
        LocalDateTime transactionDate,
        String senderCustomerId,
        String receiverCustomerId,
        String senderAccountNumber,
        String receiverAccountNumber,
        BigDecimal amount,
        String description,
        TransactionType type
) {
}
