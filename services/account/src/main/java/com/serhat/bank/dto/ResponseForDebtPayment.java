package com.serhat.bank.dto;

import java.math.BigDecimal;

public record ResponseForDebtPayment(
        Integer accountNumber,
        BigDecimal balance
) {
}
