package com.serhat.bank.service;

import com.serhat.bank.client.CustomerClient;
import com.serhat.bank.client.CustomerResponse;
import com.serhat.bank.dto.AccountRequest;
import com.serhat.bank.dto.AccountResponse;
import com.serhat.bank.model.Account;
import com.serhat.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;
    private final CustomerClient customerClient;

    public String createAccount(AccountRequest request) {

        CustomerResponse customer = customerClient.findCustomerById(request.customerId());
        if (customer == null) {
            throw new RuntimeException("Customer not found for ID: " + request.customerId());
        }

        Account account = Account.builder()
                .accountName(request.accountName())
                .currency(request.currency())
                .accountType(request.accountType())
                .balance(request.balance())
                .customerId(request.customerId())
                .build();

        Account savedAccount = repository.save(account);
        return "Account created successfully with ID: " + savedAccount.getId() + " Customer Personal Id : "+customer.personalId();
    }

    public List<AccountResponse> findAllAccounts() {
        return repository.findAll()
                .stream()
                .map(account -> {
                    CustomerResponse customer = customerClient.findCustomerById(account.getCustomerId());
                    return new AccountResponse(
                            account.getId(),
                            account.getAccountName(),
                            account.getCurrency(),
                            account.getAccountType(),
                            account.getBalance(),
                            customer
                    );
                })
                .toList();
    }

    public AccountResponse findById(Integer id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        CustomerResponse customer = customerClient.findCustomerById(account.getCustomerId());
        return new AccountResponse(
                account.getId(),
                account.getAccountName(),
                account.getCurrency(),
                account.getAccountType(),
                account.getBalance(),
                customer
        );
    }

    public String deleteAccount(Integer id) {
        repository.deleteById(id);
        return "Account deleted successfully";
    }
}
