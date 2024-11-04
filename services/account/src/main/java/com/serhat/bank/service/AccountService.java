package com.serhat.bank.service;

import com.serhat.bank.client.*;
import com.serhat.bank.dto.AccountRequest;
import com.serhat.bank.dto.AccountResponse;
import com.serhat.bank.kafka.AccountCreatedEvent;
import com.serhat.bank.kafka.Status;
import com.serhat.bank.model.Account;
import com.serhat.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository repository;
    private final CustomerClient customerClient;
    private final TransactionClient transactionClient;
    private final AccountMapper mapper;
    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    public String createAccount(AccountRequest request) {

        CustomerResponse customer = customerClient.findCustomerById(request.customerId());
        if (customer == null) {
            throw new RuntimeException("Customer not found for ID: " + request.customerId());
        }

        Account account = mapper.mapToAccount(request);
        Account savedAccount = repository.save(account);
        AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent(account.getAccountNumber(), Status.CREATED);

        log.info("Account created successfully");
        log.info("Kafka Topic sending for The Account Creation -- Started");
        kafkaTemplate.send("Account-created", accountCreatedEvent);
        log.info("Kafka Topic sending for The Account Creation -- End");
        return "Account created successfully with ID: " + savedAccount.getId() + " Account Number : " + savedAccount.getAccountNumber() + " Customer Personal Id : " + customer.personalId();
    }


    public List<AccountResponse> findAllAccounts() {
        return repository.findAll()
                .stream()
                .map(mapper::accountData)
                .toList();
    }

    // For the AccountId
    public AccountResponse findById(Integer id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapper.accountData(account);
    }

    public AccountResponse updateAccountBalance(int accountNumber, BigDecimal newBalance) {
        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for account number: " + accountNumber));

        account.setBalance(newBalance);
        Account updatedAccount = repository.save(account);
        return mapper.accountData(updatedAccount);
    }

    public DepositResponse updateBalanceAfterDeposit(DepositRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.amount()));
        repository.save(account);

        CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));
        return new DepositResponse(account.getAccountNumber(), request.description(), request.amount(), customer);
    }

    public WithdrawResponse updateBalanceAfterWithdraw(WithdrawRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.senderAccountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal newBalance = account.getBalance().subtract(request.amount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(newBalance);
        repository.save(account);

        CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.receiverCustomerId()));
        return new WithdrawResponse(account.getAccountNumber(), request.amount(), request.description(), customer);
    }


    // For the CustomerId
    public List<AccountResponse> findAccountByCustomerId(Integer customerId) {
        List<Account> accounts = repository.findByCustomerId(customerId);
        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found for customer ID: " + customerId);
        }
        return accounts.stream()
                .map(mapper::accountData)
                .toList();
    }

    public String deleteAccount(Integer id) {
        repository.deleteById(id);
        return "Account deleted successfully";
    }


    public AccountResponse findByAccountNumber(int accountNumber) {
        Optional<Account> account = repository.findByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            throw new RuntimeException("Account Not found");
        }
        return mapper.accountData(account.get());
    }




}
