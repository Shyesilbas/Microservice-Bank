package com.serhat.bank.dto;

import java.math.BigDecimal;

public record payTotalLoanDebtResponse(
        Integer loanId,
        String accountNumber,
        BigDecimal creditAmount,
        BigDecimal payback,
        BigDecimal payed,
        LoanStatus status
) {
}
