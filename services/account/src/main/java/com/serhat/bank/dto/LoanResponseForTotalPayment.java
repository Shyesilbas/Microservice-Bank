package com.serhat.bank.dto;

import java.math.BigDecimal;

public record LoanResponseForTotalPayment(
        Integer customerId,
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
