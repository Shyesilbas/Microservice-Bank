package com.serhat.creditcard.client;

import com.serhat.creditcard.repository.CreditCardRepository;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service",url = "http://localhost:8060/api/v1/accounts")
public interface AccountClient {


    @GetMapping("/byAccountNumber/{accountNumber}")
    AccountResponse findByAccountNumber(@PathVariable  String accountNumber);

    @PostMapping("/creditCards/{accountId}")
    void updateLinkedCreditCards(@PathVariable Integer accountId , @RequestBody Integer creditCardId);

}
