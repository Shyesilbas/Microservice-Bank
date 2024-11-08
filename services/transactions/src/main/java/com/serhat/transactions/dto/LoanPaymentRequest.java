package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record LoanPaymentRequest(
        Integer loanId,
        String accountNumber,
        BigDecimal amount,
        String description
) {
}
