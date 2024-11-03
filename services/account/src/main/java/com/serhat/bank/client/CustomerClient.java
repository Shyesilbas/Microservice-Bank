package com.serhat.bank.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "customer-service",url = "http://localhost:8070/api/v1/customers")
public interface CustomerClient {

    @GetMapping("/{customerId}")
    @CircuitBreaker(name = "customerServiceCircuitBreaker")
    CustomerResponse findCustomerById(@PathVariable Integer customerId);

}
