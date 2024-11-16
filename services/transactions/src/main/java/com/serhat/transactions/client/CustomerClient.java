package com.serhat.transactions.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


@FeignClient(name = "customer-service",url = "http://localhost:8070/api/v1/customers")
public interface CustomerClient {

    @GetMapping("/{customerId}")
    CustomerResponse findCustomerById(@PathVariable Integer customerId);

    @GetMapping("/accounts/{customerId}")
    List<AccountResponse> findAccountsByCustomerId(@PathVariable Integer customerId);

}
