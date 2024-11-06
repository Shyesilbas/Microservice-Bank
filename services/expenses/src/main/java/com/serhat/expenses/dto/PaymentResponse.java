package com.serhat.expenses.dto;

import com.serhat.expenses.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String cardNumber,
        String companyName,
        LocalDateTime date,
        BigDecimal amount,
        Category category
) {
}
