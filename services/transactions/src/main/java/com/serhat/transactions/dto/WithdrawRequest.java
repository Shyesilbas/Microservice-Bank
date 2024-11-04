package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record WithdrawRequest(
        String senderAccountNumber,
        String receiverAccountNumber,
        String senderCustomerId,
        String receiverCustomerId,
        BigDecimal amount,
        String description
) {
}
