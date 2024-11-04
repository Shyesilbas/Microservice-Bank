package com.serhat.bank.controller;

import java.math.BigDecimal;

public record UpdateBalanceRequest(
        String accountNumber,
        BigDecimal newBalance
) {
}
