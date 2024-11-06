package com.serhat.expenses.dto;

import com.serhat.expenses.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProcessResponse(
        String customerName,
        String companyName,
        LocalDateTime date,
        String cardNumber,
        BigDecimal amount,
        Category category
) {
}
