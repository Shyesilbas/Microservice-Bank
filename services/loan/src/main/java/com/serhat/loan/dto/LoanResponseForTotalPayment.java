package com.serhat.loan.dto;

import com.serhat.loan.entity.LoanType;

import java.math.BigDecimal;

public record LoanResponseForTotalPayment(
        String customerId,
        BigDecimal amount,
        String accountNumber,
        BigDecimal debtLeft,

        Integer installment,
        String description,
        BigDecimal payback,
        LoanType loanType,
        Integer paymentDay
) {
}
