package com.serhat.bank.fallback;

import com.serhat.bank.client.TransactionClient;
import com.serhat.bank.dto.TransactionHistory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
public class TransactionClientFallback implements TransactionClient {


    @Override
    public List<TransactionHistory> getTransactionHistory(String accountNumber) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Transaction service is temporarily unavailable.");
    }
}
