package com.serhat.bank.client;

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
