package com.serhat.transactions.kafka;

import com.serhat.transactions.entity.Status;

public record TransferEvent(
        Integer transactionId,
        Status status
) {
}
