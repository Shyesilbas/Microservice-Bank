package com.serhat.bank.dto;

import com.serhat.bank.model.Occupation;

import java.math.BigDecimal;

public record CustomerRequest(
        String personalId,
        String name,
        String surname,
        String email,
        BigDecimal monthlyIncome,
        Occupation occupation
) {
}
