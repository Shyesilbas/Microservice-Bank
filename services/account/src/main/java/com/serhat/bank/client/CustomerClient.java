package com.serhat.bank.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "customer-service",url = "http://localhost:8070/api/v1/customers")
public interface CustomerClient {

    @GetMapping("/{customerId}")
   CustomerResponse findCustomerById(@PathVariable Integer customerId);


}
