package com.serhat.bank.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomerNotFoundException extends RuntimeException {
   public CustomerNotFoundException(String message) {
      super(message);
   }
}
