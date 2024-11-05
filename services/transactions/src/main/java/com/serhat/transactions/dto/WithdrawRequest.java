package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record WithdrawRequest(
        String senderAccountNumber,
        String receiverCustomerId,
        BigDecimal amount,
        String description
) {
}
