package com.serhat.bank.service;

import com.serhat.bank.client.CustomerClient;
import com.serhat.bank.client.CustomerResponse;
import com.serhat.bank.dto.AccountRequest;
import com.serhat.bank.dto.AccountResponse;
import com.serhat.bank.model.Account;
import com.serhat.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository repository;
    private final CustomerClient customerClient;

    public String createAccount(AccountRequest request) {

        String uniqueAccountNumber = generateUniqueAccountNumber();

        CustomerResponse customer = customerClient.findCustomerById(request.customerId());
        if (customer == null) {
            throw new RuntimeException("Customer not found for ID: " + request.customerId());
        }

        Account account = Account.builder()
                .accountName(request.accountName())
                .accountNumber(Integer.valueOf(uniqueAccountNumber))
                .currency(request.currency())
                .accountType(request.accountType())
                .balance(request.balance())
                .customerId(request.customerId())
                .build();

        Account savedAccount = repository.save(account);
        log.info("Account created successfully");
        return "Account created successfully with ID: " + savedAccount.getId() +" Account Number : "+uniqueAccountNumber+ " Customer Personal Id : "+customer.personalId();
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.valueOf((long) (Math.random() * 1_000_000L));
        } while (repository.existsByAccountNumber(Integer.valueOf(accountNumber)));
        return accountNumber;
    }


    public List<AccountResponse> findAllAccounts() {
        return repository.findAll()
                .stream()
                .map(account -> {
                    CustomerResponse customer = customerClient.findCustomerById(account.getCustomerId());
                    return new AccountResponse(
                            account.getId(),
                            account.getAccountNumber(),
                            account.getAccountName(),
                            account.getCurrency(),
                            account.getAccountType(),
                            account.getBalance(),
                            customer
                    );
                })
                .toList();
    }

    // For the AccountId
    public AccountResponse findById(Integer id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        CustomerResponse customer = customerClient.findCustomerById(account.getCustomerId());
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getCurrency(),
                account.getAccountType(),
                account.getBalance(),
                customer
        );
    }

    // For the CustomerId
    public List<AccountResponse> findAccountByCustomerId(Integer customerId) {
        List<Account> accounts = repository.findByCustomerId(customerId);
        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found for customer ID: " + customerId);
        }
        return accounts.stream()
                .map(account -> {
                    CustomerResponse customer = customerClient.findCustomerById(account.getCustomerId());
                    return new AccountResponse(
                            account.getId(),
                            account.getAccountNumber(),
                            account.getAccountName(),
                            account.getCurrency(),
                            account.getAccountType(),
                            account.getBalance(),
                            customer
                    );
                })
                .toList();
    }


    public String deleteAccount(Integer id) {
        repository.deleteById(id);
        return "Account deleted successfully";
    }


}
