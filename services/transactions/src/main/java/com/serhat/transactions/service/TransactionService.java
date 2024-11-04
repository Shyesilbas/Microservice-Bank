package com.serhat.transactions.service;

import com.serhat.transactions.client.AccountClient;
import com.serhat.transactions.client.AccountResponse;
import com.serhat.transactions.client.CustomerClient;
import com.serhat.transactions.client.CustomerResponse;
import com.serhat.transactions.dto.DepositRequest;
import com.serhat.transactions.dto.DepositResponse;
import com.serhat.transactions.entity.Status;
import com.serhat.transactions.entity.Transaction;
import com.serhat.transactions.entity.TransactionType;
import com.serhat.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;


    public DepositResponse deposit(DepositRequest request){
       AccountResponse response = accountClient.findByAccountNumber(request.accountNumber());
        CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));
       if(response == null){
           throw new RuntimeException("Account Not found");
       }
        Transaction transaction = Transaction.builder()
                .senderCustomerId(null)
                .receiverCustomerId(String.valueOf(response.id()))
                .senderAccountNumber(null)
                .receiverAccountNumber(String.valueOf(response.accountNumber()))
                .description(request.description())
                .amount(request.amount())
                .status(Status.SUCCESSFUL)
                .transactionType(TransactionType.DEPOSIT)
                .transactionDate(LocalDateTime.now())
                .build();

       repository.save(transaction);
       return new DepositResponse(
               request.accountNumber(), request.description(), request.amount(),customer
       );
    }


}
