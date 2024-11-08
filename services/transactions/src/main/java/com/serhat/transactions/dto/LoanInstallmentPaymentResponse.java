package com.serhat.transactions.dto;

import java.math.BigDecimal;

public record LoanInstallmentPaymentResponse(
        BigDecimal payed,
        String accountNumber,
        BigDecimal debtLeft
) {
}
