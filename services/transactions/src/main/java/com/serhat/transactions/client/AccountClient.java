package com.serhat.transactions.client;

import com.serhat.transactions.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service",url = "http://localhost:8060/api/v1/accounts")
public interface AccountClient {


    @GetMapping("/byAccountNumber/{accountNumber}")
    AccountResponse findByAccountNumber(@PathVariable  String accountNumber);

    @PostMapping("/deposit")
    DepositResponse updateBalanceAfterDeposit (@RequestBody DepositRequest request);

    @PostMapping("/withdraw")
    WithdrawResponse updateBalanceAfterWithdraw(@RequestBody WithdrawRequest request);




}
