package com.serhat.bank.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomerHasNoAccountsException extends RuntimeException {
    public CustomerHasNoAccountsException(String message) {
        super(message);
    }
}
