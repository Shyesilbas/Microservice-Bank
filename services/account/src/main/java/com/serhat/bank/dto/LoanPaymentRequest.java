package com.serhat.bank.dto;

import java.math.BigDecimal;

public record LoanPaymentRequest(
        Integer loanId,
        String accountNumber,
        BigDecimal amount
) {
}
