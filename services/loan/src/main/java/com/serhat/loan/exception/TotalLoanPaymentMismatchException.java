package com.serhat.loan.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TotalLoanPaymentMismatchException extends RuntimeException {
    private final String errorMessage;
}
