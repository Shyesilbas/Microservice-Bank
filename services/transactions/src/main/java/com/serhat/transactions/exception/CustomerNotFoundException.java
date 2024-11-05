package com.serhat.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
@Getter

public class CustomerNotFoundException extends RuntimeException{
    private final String errorMessage;



}
