package com.serhat.loan.dto;

import java.math.BigDecimal;

public record LoanInstallmentPayRequest(
        Integer loanId,
        String accountNumber,
        BigDecimal amount
) {
}
