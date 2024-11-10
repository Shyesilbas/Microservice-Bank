package com.serhat.creditcard.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CardNotFoundException extends RuntimeException {
    private final String errorMessage;
}
