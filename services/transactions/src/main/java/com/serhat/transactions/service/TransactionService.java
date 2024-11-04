package com.serhat.transactions.service;

import com.serhat.transactions.client.*;
import com.serhat.transactions.dto.DepositRequest;
import com.serhat.transactions.dto.DepositResponse;
import com.serhat.transactions.dto.WithdrawRequest;
import com.serhat.transactions.dto.WithdrawResponse;
import com.serhat.transactions.entity.Status;
import com.serhat.transactions.entity.Transaction;
import com.serhat.transactions.entity.TransactionType;
import com.serhat.transactions.kafka.DepositEvent;
import com.serhat.transactions.kafka.WithdrawalEvent;
import com.serhat.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;
    private final KafkaTemplate<String, DepositEvent> kafkaTemplateForDeposit;
    private final KafkaTemplate<String, WithdrawalEvent> kafkaTemplateForWithdrawal;


    public DepositResponse deposit(DepositRequest request){
       AccountResponse response = accountClient.findByAccountNumber(request.accountNumber());
       CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));

       if(response == null){
           throw new RuntimeException("Account Not found");
       }
       if(customer == null){
           throw new RuntimeException("Customer NOT found");
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

        BigDecimal updatedBalance = response.balance().add(request.amount());
        response.setBalance(updatedBalance);
        accountClient.updateBalanceAfterDeposit(new DepositRequest(request.customerId(),request.accountNumber(), request.amount(), request.description()));

       repository.save(transaction);
       log.info("Transaction Type : "+transaction.getTransactionType() + " State : "+transaction.getStatus());
       log.info("Kafka Message sending for the Deposit ...");
       DepositEvent depositEvent = new DepositEvent(transaction.getTransactionId(),transaction.getStatus());
        kafkaTemplateForDeposit.send("Deposit-transaction",depositEvent);
       log.info("Kafka topic Sent successfully to topic Deposit-transaction");
       return new DepositResponse(
               request.accountNumber(), request.description(), request.amount(),customer
       );
    }

    public WithdrawResponse withdraw(WithdrawRequest request){
        CustomerResponse receiverCustomer = customerClient.findCustomerById(Integer.valueOf(request.receiverCustomerId()));
       // CustomerResponse senderCustomer = customerClient.findCustomerById(Integer.valueOf(request.senderCustomerId()));
       // AccountResponse receiverResponse = accountClient.findByAccountNumber(request.receiverAccountNumber());
        AccountResponse senderResponse = accountClient.findByAccountNumber(request.senderAccountNumber());

        if(senderResponse == null){
            throw new RuntimeException("Sender account does not exists");
        }
        if(senderResponse.balance().compareTo(request.amount())<0){
            throw new RuntimeException("Insufficient Balance!");
        }
        if (request.amount().compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException("Amount cannot be negative !");
        }
        Transaction transaction = Transaction.builder()
                .senderCustomerId(String.valueOf(receiverCustomer.id()))
                .receiverCustomerId(String.valueOf(receiverCustomer.id()))
                .senderAccountNumber(String.valueOf(senderResponse.accountNumber()))
                .receiverAccountNumber(null)
                .description(request.description())
                .amount(request.amount())
                .status(Status.SUCCESSFUL)
                .transactionType(TransactionType.WITHDRAWAL)
                .transactionDate(LocalDateTime.now())
                .build();

        BigDecimal updatedBalance = senderResponse.balance().subtract(request.amount());
        senderResponse.setBalance(updatedBalance);
        accountClient.updateBalanceAfterWithdraw(new WithdrawRequest(request.senderAccountNumber(), request.receiverAccountNumber(), request.receiverCustomerId(), request.senderCustomerId(), request.amount() ,request.description()));

        repository.save(transaction);
        log.info("Transaction Type : "+transaction.getTransactionType() + " State : "+transaction.getStatus());
        log.info("Kafka Message sending for the Withdrawal ...");
        WithdrawalEvent withdrawalEvent = new WithdrawalEvent(transaction.getTransactionId(),transaction.getStatus());
        kafkaTemplateForWithdrawal.send("Withdrawal-transaction",withdrawalEvent);
        log.info("Kafka topic Sent successfully to topic Withdrawal-transaction");
        return new WithdrawResponse(
                request.senderAccountNumber(), request.receiverAccountNumber(), request.amount(), request.description(), receiverCustomer
        );
    }

}
