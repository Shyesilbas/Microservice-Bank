package com.serhat.bank.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "customer-service",url = "http://localhost:8070/api/v1/customers")
public interface CustomerClient {

    @GetMapping("/{customerId}")
    @CircuitBreaker(name = "customerServiceCircuitBreaker")
    CustomerResponse findCustomerById(@PathVariable Integer customerId);

    @PostMapping("/accounts/{customerId}")
    void updateRelatedAccount(@PathVariable Integer customerId , @RequestBody Integer accountId);

}
