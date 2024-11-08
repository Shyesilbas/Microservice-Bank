package com.serhat.loan.dto;

import java.math.BigDecimal;

public record AccountResponse(
        BigDecimal balance,
        String accountNumber
) {
}
