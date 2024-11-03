package com.serhat.bank.dto;

import com.serhat.bank.model.Occupation;

import java.math.BigDecimal;

public record CustomerResponse(
        Integer id,
        String personalId,
        String name,
        String surname,
        String email,
        BigDecimal monthlyIncome,
        Occupation occupation
) {
}
