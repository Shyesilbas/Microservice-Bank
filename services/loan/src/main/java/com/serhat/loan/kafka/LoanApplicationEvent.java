package com.serhat.loan.kafka;

import com.serhat.loan.entity.LoanType;

import java.math.BigDecimal;

public record LoanApplicationEvent(
        String customerId,
        BigDecimal amount,
        String accountNumber,
        Integer installment,
        String description,
        LoanType loanType,
        Integer paymentDay
) {
}
