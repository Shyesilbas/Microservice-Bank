package com.serhat.expenses.client;

import com.serhat.expenses.dto.CreditCardResponse;
import com.serhat.expenses.kafka.PaymentSuccessfulEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
