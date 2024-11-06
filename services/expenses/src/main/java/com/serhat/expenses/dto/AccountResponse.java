package com.serhat.expenses.dto;

import java.math.BigDecimal;

public record AccountResponse(
        BigDecimal balance
) {
}
