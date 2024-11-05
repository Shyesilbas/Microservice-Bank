package com.serhat.bank.dto;

import com.serhat.bank.client.CustomerResponse;

import java.math.BigDecimal;

public record TransferRequest(
        String senderAccountNumber,
        String senderId,
        BigDecimal amount,
        String receiverAccountNumber,
        String receiverId,
        CustomerResponse customer
) {
}
