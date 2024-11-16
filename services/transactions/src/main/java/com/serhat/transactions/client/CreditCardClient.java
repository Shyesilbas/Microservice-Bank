package com.serhat.transactions.client;

import com.serhat.transactions.dto.CreditCardResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "credit-card-service",url = "http://localhost:8040/api/v1/creditCard")
public interface CreditCardClient {

    @GetMapping("/{cardNumber}")
    CreditCardResponse findCardByCardNumber(@PathVariable String cardNumber);


    @PutMapping ("/updateDebtAndBalance/{cardNumber}")
    void updateDebtAndBalanceAfterProcess(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal updatedDebt,
            @RequestParam BigDecimal updatedBalance
    );



}
