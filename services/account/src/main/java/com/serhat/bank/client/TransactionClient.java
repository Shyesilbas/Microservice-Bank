package com.serhat.bank.client;

import com.serhat.bank.dto.TransactionHistory;
import com.serhat.bank.fallback.TransactionClientFallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "transaction-service",
        url = "http://localhost:8050/api/v1/transactions",
        fallback = TransactionClientFallback.class,
        contextId = "transactionFeignClient"

)
public interface TransactionClient {

    @GetMapping("/transactionHistory/{accountNumber}")
    @CircuitBreaker(name = "transactionCircuitBreaker")
    List<TransactionHistory> getTransactionHistory(@PathVariable String accountNumber);

}
