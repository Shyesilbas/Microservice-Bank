package com.serhat.expenses.client;

import com.serhat.expenses.dto.AccountResponse;
import com.serhat.expenses.dto.DebtPaymentRequest;
import com.serhat.expenses.dto.WithdrawRequest;
import com.serhat.expenses.dto.WithdrawResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "account-service",url = "http://localhost:8060/api/v1/accounts")
public interface AccountClient {


    @GetMapping("/byAccountNumber/{accountNumber}")
    AccountResponse findByAccountNumber(@PathVariable  String accountNumber);

    @PutMapping("/debtPayment/{accountNumber}")
    WithdrawResponse updateBalanceAfterCardDebtPayment(@PathVariable String accountNumber , @RequestParam BigDecimal updatedBalance);


}
