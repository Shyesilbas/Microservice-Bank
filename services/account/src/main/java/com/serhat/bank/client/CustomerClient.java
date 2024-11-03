package com.serhat.bank.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "customer-service",url = "http://localhost:8070/api/v1/customers")
public interface CustomerClient {

    Logger log = LoggerFactory.getLogger(CustomerClient.class);

    @GetMapping("/{customerId}")
    @CircuitBreaker(name = "customerServiceCircuitBreaker",fallbackMethod = "fallBackFindCustomerById")
    CustomerResponse findCustomerById(@PathVariable Integer customerId);

    default CustomerResponse fallbackFindCustomerById(Integer customerId, Throwable throwable) {
        log.info("Customer id {} does not exists , error {}",customerId,throwable.getMessage());
        return new CustomerResponse(customerId,null,null,null,null);
    }


}
