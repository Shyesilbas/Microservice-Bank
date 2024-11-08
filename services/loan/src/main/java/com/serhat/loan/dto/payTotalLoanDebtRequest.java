package com.serhat.loan.dto;

import java.math.BigDecimal;

public record payTotalLoanDebtRequest(
        String accountNumber,
        Integer loanId,
        BigDecimal amount
) {
}
