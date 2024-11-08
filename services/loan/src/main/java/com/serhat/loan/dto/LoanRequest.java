package com.serhat.loan.dto;

import com.serhat.loan.entity.LoanType;

import java.math.BigDecimal;

public record LoanRequest(
        String customerId,
        BigDecimal amount,
        String accountNumber,
        Integer installment,
        String description,
        LoanType loanType,
        Integer paymentDay
) {
}
