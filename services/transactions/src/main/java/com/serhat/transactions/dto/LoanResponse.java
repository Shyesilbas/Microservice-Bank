package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record LoanResponse(
        String customerId,
        BigDecimal amount,
        String accountNumber,
        Integer installment,
        String description,
        BigDecimal payback,
        LoanType loanType,
        Integer paymentDay
) {
}
