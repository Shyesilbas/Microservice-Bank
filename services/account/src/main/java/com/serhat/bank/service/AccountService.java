package com.serhat.bank.service;

import com.serhat.bank.client.*;
import com.serhat.bank.dto.*;
import com.serhat.bank.exception.AccountNotFoundException;
import com.serhat.bank.exception.CustomerHasNoAccountsException;
import com.serhat.bank.exception.CustomerNotFoundException;
import com.serhat.bank.kafka.AccountCreatedEvent;
import com.serhat.bank.kafka.Status;
import com.serhat.bank.model.Account;
import com.serhat.bank.model.Currency;
import com.serhat.bank.repository.AccountRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository repository;
    private final CustomerClient customerClient;
    private final LoanClient loanClient;
    private final AccountMapper mapper;
    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;
    private final CacheManager cacheManager;
    private final TransactionClient transactionClient;

    public AccountResponse createAccount(AccountRequest request) {
        try {
            CustomerResponse customer = customerClient.findCustomerById(request.customerId());

            if (customer == null) {
                throw new CustomerNotFoundException("Customer not found for ID: " + request.customerId());
            }

            Account account = mapper.mapToAccount(request);
            Account savedAccount = repository.save(account);
            AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent(customer.id(),account.getAccountNumber(), Status.CREATED);
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
        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer not found for ID: " + request.customerId());
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while creating the account.");
        }
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
    @Cacheable(value = "accounts", key = "#id")
    public AccountResponse findById(Integer id) {
        Cache cache = cacheManager.getCache("accounts");
        if (cache != null && cache.get(id) != null) {
            log.info("Cache hit for account id: {}", id);
        } else {
            log.info("Cache miss for account id: {} - Loading from database", id);
        }

        Account account = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        return mapper.accountData(account);
    }

    public DepositResponse updateBalanceAfterDeposit(DepositRequest request) throws AccountNotFoundException {
        Account account = repository.findByAccountNumber(Integer.parseInt(request.accountNumber()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        account.setBalance(currentBalance.add(request.amount()));
        repository.save(account);


        CustomerResponse customer;
        try {
            customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));
        } catch (FeignException e) {
            throw new CustomerNotFoundException("Customer not found with ID: " + request.customerId());
        }


        return new DepositResponse(account.getAccountNumber(), request.description(), request.amount(), customer);
    }



    // For the CustomerId
    public List<AccountResponse> findAccountByCustomerId(Integer customerId) {
        CustomerResponse customerResponse = customerClient.findCustomerById(customerId);
        if(customerResponse == null){
            throw new CustomerNotFoundException("Customer Not found for id : "+customerId);
        }
        try {
            List<Account> accounts = repository.findByCustomerId(customerId);
            if (accounts.isEmpty()) {
                throw new CustomerHasNoAccountsException("No accounts found for customer ID: " + customerId);
            }
            return accounts.stream()
                    .map(mapper::accountData)
                    .toList();
        }catch (FeignException e){
            log.error("Error fetching accounts for customer ID {}: {}", customerId, e.getMessage());
            throw new CustomerHasNoAccountsException("Failed to fetch accounts for customer ID  ");
        }
    }

    public String deleteAccount(Integer id) {
        repository.deleteById(id);
        return "Account deleted successfully";
    }


    public AccountResponse findByAccountNumber(int accountNumber) {
        Optional<Account> account = repository.findByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Account Not found");
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


    public List<AccountResponse> findAccountsByCurrencyForSpecialCustomer(Integer customerId, Currency currency) {
        List<Account> accountResponses = repository.findAccountByCurrencyAndCustomerId(currency,customerId);
        CustomerResponse customerResponse = customerClient.findCustomerById(customerId);

        return accountResponses.stream()
                .map(account -> new AccountResponse(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getAccountName(),
                        account.getCurrency(),
                        account.getAccountType(),
                        account.getBalance(),
                        customerResponse
                ))
                .toList();
    }



}
