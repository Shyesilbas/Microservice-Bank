package com.serhat.bank.dto;

import java.math.BigDecimal;

public record LoanResponse(
        Integer customerId,
        BigDecimal amount,
        String accountNumber,
        Integer installment,
        String description,
        BigDecimal payback,
        LoanType loanType,
        Integer paymentDay
) {
}
