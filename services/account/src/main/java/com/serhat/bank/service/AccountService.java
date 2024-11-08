package com.serhat.bank.service;

import com.serhat.bank.client.*;
import com.serhat.bank.dto.*;
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
    private final LoanClient loanClient;
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
        customerClient.updateRelatedAccount(String.valueOf(request.customerId()), account.getId());

        log.info("Account created successfully");
        log.info("Kafka Topic sending for The Account Creation -- Started");
        kafkaTemplate.send("Account-created", accountCreatedEvent);
        log.info("Kafka Topic sending for The Account Creation -- End");
        return "Account created successfully with ID: " + savedAccount.getId() + " Account Number : " + savedAccount.getAccountNumber() + " Customer Personal Id : " + customer.personalId();
    }

    public ResponseForDebtPayment updateBalanceAfterCardDebtPayment(String accountNumber, BigDecimal updatedBalance) {
        Account account = repository.findByAccountNumber(Integer.parseInt(accountNumber))
                .orElseThrow(() -> new RuntimeException("Account not found with account number: " + accountNumber));


        account.setBalance(updatedBalance);
        repository.save(account);

        return new ResponseForDebtPayment(
                account.getAccountNumber(),
                account.getBalance()
        );
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

    public DepositResponse updateBalanceAfterDeposit(DepositRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.amount()));
        repository.save(account);

        CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));
        return new DepositResponse(account.getAccountNumber(), request.description(), request.amount(), customer);
    }

    /*
    public TransferRequest updateBalancesAfterTransfer(TransferRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.amount()));
        repository.save(account);

        CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));
        return new DepositResponse(account.getAccountNumber(), request.description(), request.amount(), customer);
    }
     */

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

    public void updateLinkedCreditCards(Integer accountId) {

        Account account = repository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));


        account.setRelatedCreditCard(account.getRelatedCreditCard() + 1);
        repository.save(account);
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


    public LoanResponse updateBalanceAfterLoanApplication(LoanRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.amount()));
        repository.save(account);

        CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));
        BigDecimal interestRate = BigDecimal.valueOf(0.05);
        BigDecimal totalInterest = request.amount().multiply(interestRate).multiply(BigDecimal.valueOf(request.installment()));
        BigDecimal payback = request.amount().add(totalInterest);


        return new LoanResponse(
                customer.id(),
                request.amount(),
                request.accountNumber(),
                request.installment(),
                request.description(),
                payback,
                request.loanType(),
                request.paymentDay()
        );
    }

    public LoanInstallmentPaymentResponse updateBalanceAfterLoanPayment(LoanPaymentRequest request) throws AccountNotFoundException {
        Account findAccount = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        LoanResponse loanResponse = loanClient.findByLoanId(request.loanId());

        findAccount.setBalance(findAccount.getBalance().subtract(request.amount()));

        BigDecimal totalDebt = loanResponse.amount();
        BigDecimal debtAfterPayment = totalDebt.subtract(request.amount());

        repository.save(findAccount);

        if(request.amount().compareTo(totalDebt)>0){
            throw new RuntimeException("Payment Amount cannot be higher than the debt");
        }

        return new LoanInstallmentPaymentResponse(
                request.amount(),
                request.accountNumber(),
                debtAfterPayment
        );
    }
}
