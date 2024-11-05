package com.serhat.transactions.dto;

import com.serhat.transactions.client.CustomerResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        String senderAccountNumber,
        String senderId,
        BigDecimal amount,
        String receiverAccountNumber,
        String receiverId,
        String description,
        LocalDateTime transferTime,
        String senderCustomerName,
        String receiverCustomerName

) {
}
