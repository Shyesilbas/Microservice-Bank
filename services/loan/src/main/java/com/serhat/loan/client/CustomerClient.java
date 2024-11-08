package com.serhat.loan.client;

import com.serhat.loan.dto.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "customer-service",url = "http://localhost:8070/api/v1/customers")
public interface CustomerClient {

    @GetMapping("/{customerId}")
    CustomerResponse findCustomerById(@PathVariable Integer customerId);

    @PostMapping("/account/{customerId}")
    void updateRelatedAccount(@PathVariable String customerId , @RequestBody Integer accountId);

}
