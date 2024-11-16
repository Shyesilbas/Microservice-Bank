package com.serhat.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@AllArgsConstructor
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomerHasNoAccountsException extends RuntimeException {
   public CustomerHasNoAccountsException(String message) {
      super(message);
   }
}
