package com.serhat.bank.exception;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private int status;
    private String error;
}
