package com.serhat.creditcard.client;

import com.serhat.creditcard.repository.CreditCardRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "customer-service",url = "http://localhost:8070/api/v1/customers")
public interface CustomerClient {

    @GetMapping("/{customerId}")
    @CircuitBreaker(name = "customerServiceFindCustomer")
    CustomerResponse findCustomerById(@PathVariable Integer customerId);

    @PostMapping("/creditCards/{customerId}")
    @CircuitBreaker(name = "customerServiceUpdateLinkedCreditCardsForCustomer")
    void updateLinkedCreditCards(@PathVariable String customerId , @RequestBody Integer creditCardId);



}
