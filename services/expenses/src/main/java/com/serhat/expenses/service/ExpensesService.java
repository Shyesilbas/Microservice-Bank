package com.serhat.expenses.service;

import com.serhat.expenses.Repository.ExpensesRepository;
import com.serhat.expenses.client.CreditCardClient;
import com.serhat.expenses.client.CustomerClient;
import com.serhat.expenses.dto.CreditCardResponse;
import com.serhat.expenses.dto.CustomerResponse;
import com.serhat.expenses.dto.ProcessRequest;
import com.serhat.expenses.dto.ProcessResponse;
import com.serhat.expenses.entity.Expenses;
import com.serhat.expenses.kafka.PaymentSuccessfulEvent;
import com.serhat.expenses.kafka.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpensesService {

    private final ExpensesRepository repository;
    private final CustomerClient customerClient;
    private final CreditCardClient creditCardClient;
    private final KafkaTemplate<String, PaymentSuccessfulEvent> paymentSuccessfulEventKafkaTemplate;

    public ProcessResponse doPayment(ProcessRequest request) {
        CreditCardResponse creditCardResponse = creditCardClient.findCardByCardNumber(request.cardNumber());
        CustomerResponse customer = customerClient.findCustomerById(request.customerId());

        if (creditCardResponse == null || customer == null) {
            throw new RuntimeException("Customer or Credit Card not found.");
        }

        BigDecimal balance = creditCardResponse.balance() != null ? creditCardResponse.balance() : BigDecimal.ZERO;
        BigDecimal debt = creditCardResponse.debt() != null ? creditCardResponse.debt() : BigDecimal.ZERO;

        // check if clients are fetching correct responses
        System.out.println("Card Number : "+creditCardResponse.cardNumber());
        System.out.println("Input card Number : "+request.cardNumber());
        System.out.println("Available Balance: " + balance);
        System.out.println("Requested Amount: " + request.amount());

        BigDecimal amountForProcess = request.amount();
        if (amountForProcess == null) {
            throw new RuntimeException("Requested amount is null.");
        }

        if (amountForProcess.compareTo(balance) > 0) {
            throw new RuntimeException("Insufficient Balance!");
        }


        BigDecimal updatedCardDebt = debt.add(amountForProcess);
        BigDecimal updatedCardBalance = balance.subtract(amountForProcess);

        creditCardClient.updateDebtAndBalanceAfterProcess(request.cardNumber(), updatedCardDebt, updatedCardBalance);

        Expenses expense = Expenses.builder()
                .cardNumber(request.cardNumber())
                .customerId(Integer.valueOf(customer.id()))
                .companyName(request.companyName())
                .amount(amountForProcess)
                .description(request.description())
                .category(request.category())
                .processDate(LocalDateTime.now())
                .build();

        repository.save(expense);
        log.info("Message Sending to topic Payment-successful -> Started ...");
        PaymentSuccessfulEvent paymentSuccessfulEvent = new PaymentSuccessfulEvent(expense.getId(), Status.SUCCESSFUL);
        paymentSuccessfulEventKafkaTemplate.send("Payment-successful",paymentSuccessfulEvent);
        log.info("Message Successfully Send for topic : Payment-successful -> End");

        return new ProcessResponse(
                customer.name(), request.companyName(), LocalDateTime.now(), request.cardNumber(), amountForProcess, request.category()
        );
    }



}
