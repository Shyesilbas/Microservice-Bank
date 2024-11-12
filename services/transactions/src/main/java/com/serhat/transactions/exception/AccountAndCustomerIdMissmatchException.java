package com.serhat.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccountAndCustomerIdMissmatchException extends RuntimeException {
   public AccountAndCustomerIdMissmatchException(String message) {
      super(message);
   }
}
