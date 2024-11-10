package com.serhat.bank.service;

import com.serhat.bank.client.*;
import com.serhat.bank.dto.*;
import com.serhat.bank.exception.AccountNotFoundException;
import com.serhat.bank.exception.CustomerNotFoundException;
import com.serhat.bank.exception.InsufficientBalanceException;
import com.serhat.bank.kafka.AccountCreatedEvent;
import com.serhat.bank.kafka.Status;
import com.serhat.bank.model.Account;
import com.serhat.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public AccountResponse createAccount(AccountRequest request) {

        CustomerResponse customer = customerClient.findCustomerById(request.customerId());
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found for ID: " + request.customerId());
        }

        Account account = mapper.mapToAccount(request);
        Account savedAccount = repository.save(account);
        AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent(account.getAccountNumber(), Status.CREATED);
        customerClient.updateRelatedAccount(String.valueOf(request.customerId()), account.getId());

        log.info("Account created successfully");
        log.info("Kafka Topic sending for The Account Creation -- Started");
        kafkaTemplate.send("Account-created", accountCreatedEvent);
        log.info("Kafka Topic sending for The Account Creation -- End");
        return new AccountResponse(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                request.accountName(),
                request.currency(),
                request.accountType(),
                request.balance(),
                customer
        );
    }

    public ResponseForDebtPayment updateBalanceAfterCardDebtPayment(String accountNumber, BigDecimal updatedBalance) {
        Account account = repository.findByAccountNumber(Integer.parseInt(accountNumber))
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));


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
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
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
    public WithdrawResponse updateBalanceAfterWithdraw(WithdrawRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.senderAccountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal newBalance = account.getBalance().subtract(request.amount());
        account.setBalance(newBalance);
        repository.save(account);

        CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.receiverCustomerId()));
        return new WithdrawResponse(account.getAccountNumber(), request.amount(), request.description(), customer);
    }

    public void updateLinkedCreditCards(Integer accountId) {

        Account account = repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));


        account.setRelatedCreditCard(account.getRelatedCreditCard() + 1);
        repository.save(account);
    }

    @Transactional
    public LoanResponse updateBalanceAfterLoanApplication(LoanRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal debtLeft = request.amount();
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

    @Transactional
    public LoanInstallmentPaymentResponse updateBalanceAfterLoanPayment(LoanPaymentRequest request) throws AccountNotFoundException {
        Account findAccount = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        LoanResponse loanResponse = loanClient.findByLoanId(request.loanId());

        findAccount.setBalance(findAccount.getBalance().subtract(request.amount()));

        BigDecimal totalDebt = loanResponse.amount();
        BigDecimal debtAfterPayment = totalDebt.subtract(request.amount());

        repository.save(findAccount);
        return new LoanInstallmentPaymentResponse(
                request.amount(),
                request.accountNumber(),
                debtAfterPayment
        );
    }

    @Transactional
    public payTotalLoanDebtResponse updateBalanceAfterTotalLoanPayment(payTotalLoanDebtRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        LoanResponseForTotalPayment loanResponseForTotalPayment = loanClient.findLoanById(request.loanId());
        LoanResponse loanResponse = loanClient.findByLoanId(request.loanId());

        BigDecimal paymentAmount = request.amount();
        System.out.println("Payment Amount that customer inputs : "+paymentAmount);
        BigDecimal totalDebt = loanResponseForTotalPayment.debtLeft();
        System.out.println("Total Debt left : "+totalDebt);

        account.setBalance(account.getBalance().subtract(paymentAmount));
        repository.save(account);

        return new payTotalLoanDebtResponse(
                request.loanId(),
                request.accountNumber(),
                totalDebt,
                loanResponse.payback(),
                paymentAmount,
                LoanStatus.FULLY_PAID
        );
    }






}
