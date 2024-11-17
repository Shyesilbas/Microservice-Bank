package com.serhat.transactions.client;

import com.serhat.transactions.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@FeignClient(name = "account-service",url = "http://localhost:8060/api/v1/accounts")
public interface AccountClient {


    @GetMapping("/byAccountNumber/{accountNumber}")
    @CircuitBreaker(name = "accountServiceCircuitBreaker")
    AccountResponse findByAccountNumber(@PathVariable  String accountNumber);

    @PostMapping("/deposit")
    @CircuitBreaker(name = "accountServiceCircuitBreaker")
    DepositResponse updateBalanceAfterDeposit (@RequestBody DepositRequest request);

    @PostMapping("/withdraw")
    @CircuitBreaker(name = "accountServiceCircuitBreaker")
    WithdrawResponse updateBalanceAfterWithdraw(@RequestBody WithdrawRequest request);

    @PutMapping("/debtPayment/{accountNumber}")
    @CircuitBreaker(name = "accountServiceCircuitBreaker,creditCardServiceCircuitBreaker")
    WithdrawResponse updateBalanceAfterCardDebtPayment(@PathVariable String accountNumber , @RequestParam BigDecimal updatedBalance);


    }




