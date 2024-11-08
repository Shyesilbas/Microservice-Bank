package com.serhat.bank.dto;

import java.math.BigDecimal;

public record payTotalLoanDebtRequest(
        String accountNumber,
        Integer loanId,
        BigDecimal amount
) {
}
