package com.serhat.transactions.kafka;

import com.serhat.transactions.entity.Status;

public record DepositEvent(
        Integer transactionId,
        Status status
) {
}
