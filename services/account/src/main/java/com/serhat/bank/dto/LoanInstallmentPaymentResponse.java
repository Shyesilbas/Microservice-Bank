package com.serhat.bank.dto;

import java.math.BigDecimal;

public record LoanInstallmentPaymentResponse(
        BigDecimal payed,
        String accountNumber,
        BigDecimal debtLeft
) {
}
