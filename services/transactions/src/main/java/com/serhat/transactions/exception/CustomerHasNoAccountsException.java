package com.serhat.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomerHasNoAccountsException extends RuntimeException {
   public CustomerHasNoAccountsException(String message) {
      super(message);
   }
}
