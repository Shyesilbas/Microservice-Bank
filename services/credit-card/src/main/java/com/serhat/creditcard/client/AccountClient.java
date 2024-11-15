package com.serhat.creditcard.client;

import com.serhat.creditcard.dto.CardDebtPaymentResponse;
import com.serhat.creditcard.repository.CreditCardRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "account-service",url = "http://localhost:8060/api/v1/accounts")
public interface AccountClient {


    @GetMapping("/byAccountNumber/{accountNumber}")
    @CircuitBreaker(name = "accountServiceFindAccountsCircuitBreaker")
    AccountResponse findByAccountNumber(@PathVariable  String accountNumber);

    @PostMapping("/creditCards/{accountId}")
    @CircuitBreaker(name = "accountServiceUpdateLinkedCreditCardsCircuitBreaker")
    void updateLinkedCreditCards(@PathVariable Integer accountId , @RequestBody Integer creditCardId);


    @PutMapping("/debtPayment/{accountNumber}")
    CardDebtPaymentResponse updateBalanceAfterCardDebtPayment(@PathVariable String accountNumber , @RequestParam BigDecimal updatedBalance);
}
