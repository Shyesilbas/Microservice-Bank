package com.serhat.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class ErrorResponse{
       private String message;
       private int status;
       private String error;

}