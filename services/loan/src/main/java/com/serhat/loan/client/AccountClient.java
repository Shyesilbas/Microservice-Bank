package com.serhat.loan.client;

import com.serhat.loan.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "account-service",url = "http://localhost:8060/api/v1/accounts")
public interface AccountClient {

    @PostMapping("/loan")
    LoanResponse updateBalanceAfterLoanApplication(@RequestBody LoanRequest request);

    @GetMapping("/byAccountNumber/{accountNumber}")
    AccountResponse findByAccountNumber(@PathVariable String accountNumber);

    @PostMapping("/loanPayment")
    LoanInstallmentPaymentResponse payLoanInstallment(@RequestBody LoanInstallmentPayRequest request);
}
