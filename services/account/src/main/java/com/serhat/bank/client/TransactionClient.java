package com.serhat.bank.client;

import com.serhat.bank.dto.TransactionHistory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "transaction-service",url = "http://localhost:8050/api/v1/transactions")
public interface TransactionClient {

    @GetMapping("/transactionHistory/{accountNumber}")
    List<TransactionHistory> getTransactionHistory(@PathVariable String accountNumber);

}
