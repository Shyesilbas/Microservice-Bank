package com.serhat.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BadGatewayException extends RuntimeException{
    public BadGatewayException(String message) {
        super(message);
    }
}
