package com.serhat.transactions.dto;

import com.serhat.transactions.client.CustomerResponse;

import java.math.BigDecimal;

public record TransferRequest(
        String senderAccountNumber,
        String senderId,
        BigDecimal amount,
        String receiverAccountNumber,
        String receiverId,
        String description
) {
}
