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
import com.serhat.transactions.notification.MailService;
import com.serhat.transactions.repository.TransactionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;

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
    private final MailService mailService;
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
        try {
            AccountResponse accountResponse = accountClient.findByAccountNumber(accountNumber);

            if(accountResponse == null) {
                throw new AccountNotFoundException("Account not found with account number: " + accountNumber);
            }

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
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException("Account not found with account number: " + accountNumber);
        } catch (FeignException e) {
            if (e.status() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Account service is temporarily unavailable. Please try again later.");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while communicating with the account service.");
        }
    }





    @Transactional
    public DepositResponse deposit(DepositRequest request) {
        try {
            AccountResponse response = accountClient.findByAccountNumber(request.accountNumber());
            CustomerResponse customer = customerClient.findCustomerById(Integer.valueOf(request.customerId()));

            if (response == null) {
                log.error("Account not found for account number: {}", request.accountNumber());
                throw new AccountNotFoundException("Account not found for account number: " + request.accountNumber());
            }
            if (customer == null) {
                log.error("Customer not found for customer ID: {}", request.customerId());
                throw new CustomerNotFoundException("Customer not found for customer ID: " + request.customerId());
            }
            if (!response.customer().id().equals(request.customerId())) {
                log.error("Account with number {} does not belong to customer with ID {}", request.accountNumber(), request.customerId());
                throw new AccountAndCustomerIdMissmatchException("Account with number " + request.accountNumber() +
                        " does not belong to customer with ID " + request.customerId());
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
            log.info("Transaction Type: {} State: {}", transaction.getTransactionType(), transaction.getStatus());
            log.info("Sending Kafka message for the Deposit...");

            DepositEvent depositEvent = new DepositEvent(transaction.getTransactionId(), transaction.getStatus());
            kafkaTemplateForDeposit.send("Deposit-transaction", depositEvent);
            log.info("Kafka topic sent successfully to topic Deposit-transaction");

            Currency currency = accountClient.findByAccountNumber(request.accountNumber()).currency();

            Context context = new Context();
            context.setVariable("customerName", customer.name() + " " + customer.surname());
            context.setVariable("amount", request.amount());
            context.setVariable("accountNumber", request.accountNumber());
            context.setVariable("currency", currency);
            context.setVariable("updatedBalance", updatedBalance);

            String emailSubject = "Deposit Successful";
            mailService.sendEmail(customer.email(), emailSubject, "deposit-notification", context);

            return new DepositResponse(
                    request.accountNumber(), request.description(), request.amount(), customer.id(), updatedBalance
            );

        } catch (FeignException e) {
            log.error("Feign exception occurred: {}", e.getMessage());
            if (e.status() == 404) {
                if (e.request().url().contains("customers")) {
                    throw new CustomerNotFoundException("Customer not found with ID: " + request.customerId());
                } else if (e.request().url().contains("accounts")) {
                    throw new AccountNotFoundException("Account not found with number: " + request.accountNumber());
                }
                else if (e.status() == 500 && e.request().url().contains("customers/accounts")) {
                    throw new CustomerHasNoAccountsException("Customer has no active accounts for customer ID: " + request.customerId());
                }
            }
                log.error("Unexpected Feign exception: {}", e.getMessage());
                throw new RuntimeException("Unexpected error occurred during the transaction", e);

        } catch (AccountNotFoundException | CustomerNotFoundException |
                 AccountAndCustomerIdMissmatchException e) {
            log.error("Business logic error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred: " + e.getMessage(), e);
        }
    }




    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request) {
        try {
            CustomerResponse receiverCustomer = customerClient.findCustomerById(Integer.valueOf(request.receiverCustomerId()));
            if (receiverCustomer == null) {
                throw new CustomerNotFoundException("Customer Not found with ID: " + request.receiverCustomerId());
            }

            AccountResponse senderResponse = accountClient.findByAccountNumber(request.senderAccountNumber());
            if (senderResponse == null) {
                throw new AccountNotFoundException("Account Not found for account number: " + request.senderAccountNumber());
            }

            if (!senderResponse.customer().id().equals(request.receiverCustomerId())) {
                throw new AccountAndCustomerIdMissmatchException("Account with number " + request.senderAccountNumber() + " does not belong to the specified customer with ID: " + request.receiverCustomerId());
            }

            if (senderResponse.balance().compareTo(request.amount()) < 0) {
                throw new InsufficientBalanceException("Insufficient Balance! Current balance: " + senderResponse.balance() + ", Requested withdrawal: " + request.amount());
            }
            if (request.amount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalAmountException("Amount cannot be negative! Provided amount: " + request.amount());
            }

            Transaction transaction = Transaction.builder()
                    .senderCustomerId((senderResponse.customer().id()))
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

            accountClient.updateBalanceAfterWithdraw(new WithdrawRequest(
                    request.senderAccountNumber(),
                    request.receiverCustomerId(),
                    request.amount(),
                    request.description()
            ));

            repository.save(transaction);
            log.info("Transaction Type: {} | State: {}", transaction.getTransactionType(), transaction.getStatus());
            log.info("Kafka Message sending for the Withdrawal ...");

            WithdrawalEvent withdrawalEvent = new WithdrawalEvent(transaction.getTransactionId(), transaction.getStatus());
            kafkaTemplateForWithdrawal.send("Withdrawal-transaction", withdrawalEvent);
            log.info("Kafka topic Sent successfully to topic Withdrawal-transaction");

            Currency currency = accountClient.findByAccountNumber(request.senderAccountNumber()).currency();

            Context context = new Context();
            context.setVariable("customerName", senderResponse.customer().name() + " " + senderResponse.customer().surname());
            context.setVariable("amount", request.amount());
            context.setVariable("accountNumber", request.senderAccountNumber());
            context.setVariable("currency", currency);
            context.setVariable("updatedBalance", updatedBalance);

            String emailSubject = "Withdrawal Successful";
            mailService.sendEmail(receiverCustomer.email(), emailSubject, "withdrawal-notification", context);

            return new WithdrawResponse(
                    request.senderAccountNumber(),
                    request.amount(),
                    request.description(),
                    receiverCustomer.name(),
                    receiverCustomer.surname(),
                    updatedBalance
            );

        } catch (FeignException e) {
            log.error("Feign exception occurred: {}", e.getMessage());
            if (e.status() == 404) {
                if (e.request().url().contains("customers")) {
                    throw new CustomerNotFoundException("Customer not found with ID: " + request.receiverCustomerId());
                } else if (e.request().url().contains("accounts")) {
                    throw new AccountNotFoundException("Account not found with number: " + request.senderAccountNumber());
                }
                else if (e.status() == 500 && e.request().url().contains("customers/accounts")) {
                    throw new CustomerHasNoAccountsException("Customer has no active accounts for customer ID: " + request.receiverCustomerId());
                }
            }
            log.error("Unexpected Feign exception: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred during the transaction", e);

        } catch (AccountNotFoundException | CustomerNotFoundException |
                 AccountAndCustomerIdMissmatchException e) {
            log.error("Business logic error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred: " + e.getMessage(), e);
        }

    }


@Transactional
    public TransferResponse transfer(TransferRequest request) {
        try {

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
                throw new CurrencyMismatchBetweenAccountsException("Currency Mismatch Between accounts");
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


            log.info("Kafka Message sending for the Transfer ...");
            TransferEvent transferEvent = new TransferEvent(transaction.getTransactionId(), transaction.getStatus());
            kafkaTemplateForTransfer.send("Transfer-transaction", transferEvent);
            log.info("Kafka topic Sent successfully to topic Transfer-transaction");


            Currency currency = accountClient.findByAccountNumber(request.senderAccountNumber()).currency();

            Context context = new Context();
            context.setVariable("customerName", senderCustomer.name() + " " + senderCustomer.surname());
            context.setVariable("amount", request.amount());
            context.setVariable("accountNumberSender", request.senderAccountNumber());
            context.setVariable("accountNumberReceiver", request.receiverAccountNumber());
            context.setVariable("currency", currency);
            context.setVariable("updatedBalance", updatedSenderBalance);

            String emailSubject = "Transfer Successful";
            mailService.sendEmail(senderCustomer.email(), emailSubject, "transferSenderEmail-notification", context);


            Context context2 = new Context();
            context2.setVariable("customerName", receiverCustomer.name() + " " + receiverCustomer.surname());
            context2.setVariable("amount", request.amount());
            context2.setVariable("accountNumberReceiver",receiverAccount.accountNumber());
            context2.setVariable("accountNumberSender", senderAccount.accountNumber());
            context2.setVariable("currency", currency);
            context2.setVariable("updatedBalance", updatedReceiverBalance);

            mailService.sendEmail(receiverCustomer.email(), emailSubject, "transferReceiverEmail-notification", context2);



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

        }  catch (FeignException e) {
            log.error("Feign exception occurred: {}", e.getMessage());
            if (e.status() == 404) {
                if (e.request().url().contains("customers")) {
                        throw new CustomerNotFoundException("Check the Customer Id's");
                } else if (e.request().url().contains("accounts")) {
                    throw new AccountNotFoundException("Check the Account numbers");
                }
                else if (e.status() == 500 && e.request().url().contains("customers/accounts")) {
                    throw new CustomerHasNoAccountsException("Customer has no active accounts ");
                }else if(e.status() == 500 && e.request().url().contains("customers/accounts")){
                    throw new CurrencyMismatchBetweenAccountsException("Currency Must be equal to do transfer!");
                }
            }
            log.error("Unexpected Feign exception: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred during the transaction", e);

        } catch (AccountNotFoundException | CustomerNotFoundException |
                 AccountAndCustomerIdMissmatchException | CurrencyMismatchBetweenAccountsException e) {
            log.error("Business logic error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred: " + e.getMessage(), e);
        }
    }


    @Transactional
    public LoanResponse updateTransactionHistoryAfterLoanApplication(LoanRequest request) {

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
                request.paymentDay(),
                request.installment(),
                payback
        );
    }

    @Transactional
    public payTotalLoanDebtResponse updateTransactionHistoryAfterPayTotalLoanDebt(payTotalLoanDebtRequest request){
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

    public CardDebtPaymentResponse updateTransactionHistoryAfterCardDebtPayment(CardDebtPaymentRequest request) {
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


    public LoanInstallmentPaymentResponse updateTransactionHistoryAfterLoanInstallmentPayment(LoanInstallmentPayRequest request) {
        CreditCardResponse creditCardResponse = creditCardClient.findCardByCardNumber(request.accountNumber());
        CustomerResponse customerResponse = customerClient.findCustomerById(request.customerId());
        LoanResponse loanResponse = loanClient.findByLoanId(request.loanId());
        if(creditCardResponse == null) {
            throw new RuntimeException("Card Not Found");
        }
        if(customerResponse == null){
            throw new RuntimeException("Customer Not Found");

        }
        Transaction transaction = Transaction.builder()
                .senderCustomerId(String.valueOf(request.customerId()))
                .receiverCustomerId("Credit Card - Bank")
                .senderAccountNumber(request.accountNumber())
                .receiverAccountNumber("Credit Card - Bank")
                .description(request.description())
                .amount(request.amount())
                .status(Status.SUCCESSFUL)
                .transactionType(TransactionType.CARD_DEBT_PAYMENT)
                .transactionDate(LocalDateTime.now())
                .build();

        repository.save(transaction);

        return new LoanInstallmentPaymentResponse(
                request.amount(),
                request.accountNumber(),
                loanResponse.debtLeft(),
                loanResponse.installmentLeft()
        );
    }
}
