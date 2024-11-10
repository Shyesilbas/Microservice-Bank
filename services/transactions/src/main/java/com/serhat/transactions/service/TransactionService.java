package com.serhat.transactions.service;

import com.serhat.transactions.dto.*;
import com.serhat.transactions.client.*;

import com.serhat.transactions.entity.Status;
import com.serhat.transactions.entity.Transaction;
import com.serhat.transactions.entity.TransactionType;
import com.serhat.transactions.exception.*;
import com.serhat.transactions.kafka.DepositEvent;
import com.serhat.transactions.kafka.TransferEvent;
import com.serhat.transactions.kafka.WithdrawalEvent;
import com.serhat.transactions.repository.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;
    private final CreditCardClient creditCardClient;
    private final LoanClient loanClient;
    private final KafkaTemplate<String, DepositEvent> kafkaTemplateForDeposit;
    private final KafkaTemplate<String, WithdrawalEvent> kafkaTemplateForWithdrawal;
    private final KafkaTemplate<String, TransferEvent> kafkaTemplateForTransfer;


    public List<DepositHistory> depositHistories(String accountNumber){
        return repository.findByReceiverAccountNumberAndTransactionType(accountNumber,TransactionType.DEPOSIT)
                .stream()
                .map(transaction -> new DepositHistory(
                        transaction.getReceiverAccountNumber(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTransactionDate()
                ))
                .toList();
    }

    public List<WithdrawHistory> withdrawHistories(String accountNumber){
        return repository.findByReceiverAccountNumberAndTransactionType(accountNumber,TransactionType.WITHDRAWAL)
                .stream()
                .map(transaction -> new WithdrawHistory(
                        transaction.getReceiverAccountNumber(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTransactionDate()
                ))
                .toList();
    }

    public List<TransactionHistory> transactionHistories(String accountNumber){
       return repository.findAllTransactions(accountNumber)
                .stream()
                .map(transaction -> new TransactionHistory(
                        transaction.getTransactionDate(),
                        transaction.getSenderCustomerId(),
                        transaction.getReceiverCustomerId(),
                        transaction.getSenderAccountNumber(),
                        transaction.getReceiverAccountNumber(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTransactionType()
                ))
                .toList();
    }


    @Transactional
    public DepositResponse deposit(DepositRequest request) {
        try {
            AccountResponse response = accountClient.findByAccountNumber(request.accountNumber());
            CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));
            List<AccountResponse> accountResponse = customerClient.findAccountsByCustomerId(Integer.valueOf(request.customerId()));

            if (response == null) {
                throw new AccountNotFoundException("Account Not found");
            }
            if (customer == null) {
                throw new CustomerNotFoundException("Customer Not found");
            }
            if (accountResponse == null || accountResponse.isEmpty()) {
                throw new CustomerHasNoAccountsException("Customer has no active accounts");
            }
            if (!response.customer().id().equals(request.customerId())) {
                throw new AccountAndCustomerIdMissmatchException("Account does not belong to the specified customer");
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

            accountClient.updateBalanceAfterDeposit(new DepositRequest(request.customerId(), request.accountNumber(), request.amount(), request.description()));

            repository.save(transaction);
            log.info("Transaction Type : " + transaction.getTransactionType() + " State : " + transaction.getStatus());
            log.info("Kafka Message sending for the Deposit ...");

            DepositEvent depositEvent = new DepositEvent(transaction.getTransactionId(), transaction.getStatus());
            kafkaTemplateForDeposit.send("Deposit-transaction", depositEvent);
            log.info("Kafka topic Sent successfully to topic Deposit-transaction");

            return new DepositResponse(
                    request.accountNumber(), request.description(), request.amount(), customer.id(), updatedBalance
            );

        } catch (AccountNotFoundException | CustomerNotFoundException | CustomerHasNoAccountsException | AccountAndCustomerIdMissmatchException e) {
            log.error("Error occurred: {}", e.getMessage());
            throw e;
        } catch (FeignException e) {
            log.error("Feign exception occurred: {}", e.getMessage());
            throw new RuntimeException("Service communication error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred: " + e.getMessage(), e);
        }
    }

    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request){
        CustomerResponse receiverCustomer = customerClient.findCustomerById(Integer.valueOf(request.receiverCustomerId()));
       // CustomerResponse senderCustomer = customerClient.findCustomerById(Integer.valueOf(request.senderCustomerId()));
       // AccountResponse receiverResponse = accountClient.findByAccountNumber(request.receiverAccountNumber());
        AccountResponse senderResponse = accountClient.findByAccountNumber(request.senderAccountNumber());
        List<AccountResponse> accountResponse =  customerClient.findAccountsByCustomerId(Integer.valueOf(request.receiverCustomerId()));

        if(!senderResponse.customer().id().equals(request.receiverCustomerId())){
            throw new AccountAndCustomerIdMissmatchException("Account does not belong to the specified customer");
        }
        if(senderResponse.accountNumber()== null){
            throw new WrongAccountNumberException("Check your Account Number ");
        }
        if(senderResponse.balance().compareTo(request.amount())<0){
            throw new InsufficientBalanceException("Insufficient Balance!");
        }
        if (request.amount().compareTo(BigDecimal.ZERO)<0){
            throw new IllegalAmountException("Amount cannot be negative !");
        }
        if(accountResponse == null){
          throw new CustomerHasNoAccountsException("This customer has no active accounts.");
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
        accountClient.updateBalanceAfterWithdraw(
                new WithdrawRequest(
                        request.senderAccountNumber(),
                        request.receiverCustomerId(),
                        request.amount()
                        ,request.description()));

        repository.save(transaction);
        log.info("Transaction Type : "+transaction.getTransactionType() + " State : "+transaction.getStatus());
        log.info("Kafka Message sending for the Withdrawal ...");
        WithdrawalEvent withdrawalEvent = new WithdrawalEvent(transaction.getTransactionId(),transaction.getStatus());
        kafkaTemplateForWithdrawal.send("Withdrawal-transaction",withdrawalEvent);
        log.info("Kafka topic Sent successfully to topic Withdrawal-transaction");
        return new WithdrawResponse(
                request.senderAccountNumber(),
                request.amount(),
                request.description(),
                receiverCustomer.name(),
                receiverCustomer.surname(),
                updatedBalance
        );
    }
@Transactional
    public TransferResponse transfer(TransferRequest request) {
        try {
            // Retrieve customer and account information
            CustomerResponse receiverCustomer = customerClient.findCustomerById(Integer.valueOf(request.receiverId()));
            CustomerResponse senderCustomer = customerClient.findCustomerById(Integer.valueOf(request.senderId()));
            AccountResponse receiverAccount = accountClient.findByAccountNumber(request.receiverAccountNumber());
            AccountResponse senderAccount = accountClient.findByAccountNumber(request.senderAccountNumber());

            // Validate that accounts exist and belong to the correct customers
            if (senderAccount == null) {
                throw new WrongAccountNumberException("Sender Account number does not exist. Check it");
            }
            if (receiverAccount == null) {
                throw new WrongAccountNumberException("Receiver Account number does not exist. Check it");
            }
            if (!senderAccount.customer().id().equals(request.senderId())) {
                throw new AccountAndCustomerIdMissmatchException("Sender account does not belong to the specified customer");
            }
            if (!receiverAccount.customer().id().equals(request.receiverId())) {
                throw new AccountAndCustomerIdMissmatchException("Receiver account does not belong to the specified customer");
            }
            if (!senderAccount.currency().equals(receiverAccount.currency())) {
                throw new RuntimeException("Currency Mismatch Between accounts");
            }
            if (senderAccount.balance().compareTo(request.amount()) < 0) {
                throw new InsufficientBalanceException("Insufficient Balance!");
            }

            // Create a new transaction
            Transaction transaction = Transaction.builder()
                    .senderCustomerId(String.valueOf(senderCustomer.id()))
                    .receiverCustomerId(String.valueOf(receiverCustomer.id()))
                    .senderAccountNumber(String.valueOf(senderAccount.accountNumber()))
                    .receiverAccountNumber(String.valueOf(receiverAccount.accountNumber()))
                    .description(request.description())
                    .amount(request.amount())
                    .status(Status.SUCCESSFUL)
                    .transactionType(TransactionType.TRANSFER)
                    .transactionDate(LocalDateTime.now())
                    .build();

            // Update the account balances
            BigDecimal updatedSenderBalance = senderAccount.balance().subtract(request.amount());
            senderAccount.setBalance(updatedSenderBalance);
            BigDecimal updatedReceiverBalance = receiverAccount.balance().add(request.amount());
            receiverAccount.setBalance(updatedReceiverBalance);

            // Update the balances in the respective accounts
            accountClient.updateBalanceAfterWithdraw(new WithdrawRequest(
                    request.senderAccountNumber(),
                    request.senderId(),
                    request.amount(),
                    request.description()));

            accountClient.updateBalanceAfterDeposit(new DepositRequest(
                    request.receiverId(),
                    request.receiverAccountNumber(),
                    request.amount(),
                    request.description()));


            repository.save(transaction);
            log.info("Transaction Type: {} State: {}", transaction.getTransactionType(), transaction.getStatus());

            // Send Kafka message

            log.info("Kafka Message sending for the Transfer ...");
            TransferEvent transferEvent = new TransferEvent(transaction.getTransactionId(), transaction.getStatus());
            kafkaTemplateForTransfer.send("Transfer-transaction", transferEvent);
            log.info("Kafka topic Sent successfully to topic Transfer-transaction");


            // Return the response
            return new TransferResponse(
                    request.senderAccountNumber(),
                    request.senderId(),
                    request.amount(),
                    request.receiverAccountNumber(),
                    request.receiverId(),
                    request.description(),
                    LocalDateTime.now(),
                    senderCustomer.name(),
                    receiverCustomer.name()
            );

        } catch (FeignException e) {
            log.error("Feign exception occurred: {}", e.getMessage());
            throw new RuntimeException("Service communication error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred: " + e.getMessage(), e);
        }
    }


    @Transactional
    public LoanResponse loan(LoanRequest request) {

        Transaction transaction = Transaction.builder()
                .senderCustomerId("Bank")
                .receiverCustomerId(request.customerId())
                .senderAccountNumber("Bank")
                .receiverAccountNumber(request.accountNumber())
                .description(request.description())
                .amount(request.amount())
                .status(Status.SUCCESSFUL)
                .transactionType(TransactionType.LOAN)
                .transactionDate(LocalDateTime.now())
                .build();

        BigDecimal monthlyInterestRate = new BigDecimal("0.05");
        BigDecimal principal = request.amount();
        Integer installments = request.installment();
        BigDecimal payback = principal
                .multiply(monthlyInterestRate.add(BigDecimal.ONE).pow(installments, MathContext.DECIMAL64))
                .setScale(2, RoundingMode.HALF_UP);



        repository.save(transaction);

        return new LoanResponse(
                request.customerId(),
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
    public LoanInstallmentPaymentResponse payLoanInstallment(LoanPaymentRequest request){
        LoanResponse loanResponse = loanClient.findByLoanId(request.loanId());
        Transaction transaction = Transaction.builder()
                .senderCustomerId("Customer")
                .receiverCustomerId("Bank")
                .senderAccountNumber(request.accountNumber())
                .receiverAccountNumber("Bank")
                .description(request.description())
                .amount(request.amount())
                .status(Status.SUCCESSFUL)
                .transactionType(TransactionType.LOAN_DEBT_PAYMENT)
                .transactionDate(LocalDateTime.now())
                .build();

        // Get total Debt
        BigDecimal totalDebt = loanResponse.amount();
        BigDecimal debtAfterPayment = totalDebt.subtract(request.amount());

        if(request.amount().compareTo(totalDebt)>0){
            throw new RuntimeException("Payment Amount cannot be higher than the debt");
        }

        repository.save(transaction);

        return new LoanInstallmentPaymentResponse(
                request.amount(),
                request.accountNumber(),
                debtAfterPayment
        );
    }

    @Transactional
    public payTotalLoanDebtResponse payTotalLoanDebt(payTotalLoanDebtRequest request){
        LoanResponse loanResponse = loanClient.findByLoanId(request.loanId());
        Transaction transaction = Transaction.builder()
                .senderCustomerId("Customer")
                .receiverCustomerId("Bank")
                .senderAccountNumber(request.accountNumber())
                .receiverAccountNumber("Bank")
                .description(request.description())
                .amount(request.amount())
                .status(Status.SUCCESSFUL)
                .transactionType(TransactionType.LOAN_DEBT_PAYMENT)
                .transactionDate(LocalDateTime.now())
                .build();

        repository.save(transaction);

        return new payTotalLoanDebtResponse(
                request.loanId(),
                request.accountNumber(),
                loanResponse.amount(),
                loanResponse.payback(),
                request.amount(),
                LoanStatus.FULLY_PAID
        );
    }

    public CardDebtPaymentResponse updateTransactionHistory(CardDebtPaymentRequest request) {
        CreditCardResponse creditCardResponse = creditCardClient.findCardByCardNumber(request.cardNumber());
        CustomerResponse customerResponse = customerClient.findCustomerById(request.customerId());
        if(creditCardResponse == null) {
            throw new RuntimeException("Card Not Found");
        }
            Transaction transaction = Transaction.builder()
                    .senderCustomerId(String.valueOf(request.customerId()))
                    .receiverCustomerId("Bank")
                    .senderAccountNumber(request.accountNumber())
                    .receiverAccountNumber("Bank")
                    .description(request.description())
                    .amount(request.amount())
                    .status(Status.SUCCESSFUL)
                    .transactionType(TransactionType.CARD_DEBT_PAYMENT)
                    .transactionDate(LocalDateTime.now())
                    .build();

            repository.save(transaction);

            return new CardDebtPaymentResponse(
                    customerResponse.id(),
                    request.accountNumber(),
                    request.cardNumber(),
                    request.amount(),
                    creditCardResponse.debt(),
                    creditCardResponse.balance()
            );
    }
}
