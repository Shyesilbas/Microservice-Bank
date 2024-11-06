package com.serhat.expenses.dto;

import com.serhat.expenses.entity.Category;

import java.math.BigDecimal;

public record ProcessRequest(
        Integer customerId,
        String cardNumber,
        String description,
        String cvv,
        BigDecimal amount,
        Category category,
        String companyName
) {
}
