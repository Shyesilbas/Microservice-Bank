package com.serhat.transactions.kafka;

import com.serhat.transactions.entity.Status;

public record WithdrawalEvent(
        Integer transactionId,
        Status status
) {
}
