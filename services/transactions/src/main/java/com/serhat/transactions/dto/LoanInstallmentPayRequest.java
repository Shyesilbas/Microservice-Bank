package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record LoanInstallmentPayRequest(
        Integer customerId,
        Integer loanId,
        String description,
        String accountNumber,
        BigDecimal amount
) {
}
