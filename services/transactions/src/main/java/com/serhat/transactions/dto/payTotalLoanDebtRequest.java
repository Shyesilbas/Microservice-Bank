package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record payTotalLoanDebtRequest(
        String accountNumber,
        Integer loanId,
        BigDecimal amount,
        String description
) {
}
