package com.serhat.creditcard.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentRequestMismatchException extends RuntimeException {
   private final String errorMessage;
}