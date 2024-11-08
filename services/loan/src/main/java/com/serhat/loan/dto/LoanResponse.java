package com.serhat.loan.dto;

import com.serhat.loan.entity.ApplicationStatus;
import com.serhat.loan.entity.LoanType;

import java.math.BigDecimal;

public record LoanResponse(

        String customerId,
        BigDecimal amount,
        String accountNumber,
        Integer installment,
        String description,
        BigDecimal payback,
        LoanType loanType,
        Integer paymentDay,
        ApplicationStatus status
) {
}
