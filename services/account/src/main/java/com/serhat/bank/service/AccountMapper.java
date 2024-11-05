package com.serhat.bank.service;

import com.serhat.bank.client.CustomerClient;
import com.serhat.bank.client.CustomerResponse;
import com.serhat.bank.dto.AccountRequest;
import com.serhat.bank.dto.AccountResponse;
import com.serhat.bank.model.Account;
import com.serhat.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountMapper {
    private final AccountRepository repository;
    private final CustomerClient customerClient;


    public Account mapToAccount(AccountRequest request){
        String uniqueAccountNumber = generateUniqueAccountNumber();
        if(request == null){
            throw new IllegalArgumentException("Request cannot be null");
        }
        return Account.builder()
                .accountName(request.accountName())
                .accountNumber(Integer.parseInt(uniqueAccountNumber))
                .accountType(request.accountType())
                .customerId(request.customerId())
                .balance(request.balance())
                .currency(request.currency())
                .relatedCreditCard(0)
                .build();
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.valueOf((long) (Math.random() * 1_000_000L));
        } while (repository.existsByAccountNumber(Integer.parseInt(accountNumber)));
        return accountNumber;
    }

    public AccountResponse accountData(Account account){
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
}
