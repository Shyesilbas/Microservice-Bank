package com.serhat.expenses.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InsufficientBalanceException extends RuntimeException {
    private final String errorMessage;
}
