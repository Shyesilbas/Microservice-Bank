package com.serhat.transactions.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawHistory(
        String accountNumber,
        BigDecimal amount,
        String description,
        LocalDateTime transactionDate
) {
}
