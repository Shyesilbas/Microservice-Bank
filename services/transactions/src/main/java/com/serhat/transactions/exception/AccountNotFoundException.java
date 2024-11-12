package com.serhat.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccountNotFoundException extends RuntimeException {
   public AccountNotFoundException(String message) {
      super(message);
   }

}
