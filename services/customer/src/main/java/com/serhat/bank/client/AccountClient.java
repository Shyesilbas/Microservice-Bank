package com.serhat.bank.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "account-service",url = "http://localhost:8060/api/v1/accounts")
public interface AccountClient {


    @GetMapping("/byCustomer/{customerId}")
    List<AccountResponse> findAccountsByCustomerId(@PathVariable Integer customerId);

}
