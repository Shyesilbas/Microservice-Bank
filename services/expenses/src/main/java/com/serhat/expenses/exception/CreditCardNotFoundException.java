package com.serhat.expenses.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreditCardNotFoundException extends RuntimeException {
    private final String errorMessage;
}
