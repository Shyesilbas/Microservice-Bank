package com.serhat.expenses.service;

import com.serhat.expenses.Repository.ExpensesRepository;
import com.serhat.expenses.client.AccountClient;
import com.serhat.expenses.client.CreditCardClient;
import com.serhat.expenses.client.CustomerClient;
import com.serhat.expenses.dto.*;
import com.serhat.expenses.entity.Expenses;
import com.serhat.expenses.exception.CreditCardNotFoundException;
import com.serhat.expenses.exception.CustomerNotFoundException;
import com.serhat.expenses.exception.InsufficientBalanceException;
import com.serhat.expenses.exception.NegativeAmountException;
import com.serhat.expenses.kafka.PaymentSuccessfulEvent;
import com.serhat.expenses.kafka.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpensesService {

    private final ExpensesRepository repository;
    private final CustomerClient customerClient;
    private final CreditCardClient creditCardClient;
    private final KafkaTemplate<String, PaymentSuccessfulEvent> paymentSuccessfulEventKafkaTemplate;

    @Transactional
    public ProcessResponse doPayment(ProcessRequest request) {
        CreditCardResponse creditCardResponse = creditCardClient.findCardByCardNumber(request.cardNumber());
        CustomerResponse customer = customerClient.findCustomerById(request.customerId());
        log.info("Credit Card Holder Name : "+creditCardResponse.customerName());
        log.info("Credit Card Holder Id From Credit Card Response : "+creditCardResponse.customerId());
        log.info("Credit Card Holder Id : "+request.customerId());

        if (creditCardResponse == null) {
            throw new CreditCardNotFoundException("Credit Card not found.");
        }
        if(customer == null){
            throw new CustomerNotFoundException("Customer Not Found");
        }
        if(!creditCardResponse.customerId().equals(request.customerId())){
            throw new RuntimeException("Credit Card Holder id is wrong");
        }


        BigDecimal balance = creditCardResponse.balance() != null ? creditCardResponse.balance() : BigDecimal.ZERO;
        BigDecimal debt = creditCardResponse.debt() != null ? creditCardResponse.debt() : BigDecimal.ZERO;


        System.out.println("Card Number : " + creditCardResponse.cardNumber());
        System.out.println("Input card Number : " + request.cardNumber());
        System.out.println("Available Balance: " + balance);
        System.out.println("Requested Amount: " + request.amount());

        BigDecimal amountForProcess = request.amount();
        if (amountForProcess == null) {
            throw new NegativeAmountException("Requested amount is null.");
        }

        if (amountForProcess.compareTo(balance) > 0) {
            throw new InsufficientBalanceException("Insufficient Balance!");
        }


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
        PaymentSuccessfulEvent paymentSuccessfulEvent = new PaymentSuccessfulEvent(creditCardResponse.cardNumber(),expense.getId(),request.amount() ,Status.SUCCESSFUL);
        paymentSuccessfulEventKafkaTemplate.send("Payment-successful", paymentSuccessfulEvent);
        log.info("Message Successfully Send for topic : Payment-successful -> End");

        return new ProcessResponse(
                customer.name(), request.companyName(), LocalDateTime.now(), request.cardNumber(), amountForProcess, request.category()
        );
    }



    public List<PaymentResponse> paymentHistoryByCustomerId(Integer customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(expenses -> new PaymentResponse(
                        expenses.getCardNumber(),
                        expenses.getCompanyName(),
                        expenses.getProcessDate(),
                        expenses.getAmount(),
                        expenses.getCategory()
                ))
                .toList();
    }

    public List<PaymentResponse> paymentHistoryByCardNumber(String cardNumber) {
           return repository.findByCardNumber(cardNumber)
                   .stream()
                   .map(expenses -> new PaymentResponse(
                           expenses.getCardNumber(),
                           expenses.getCompanyName(),
                           expenses.getProcessDate(),
                           expenses.getAmount(),
                           expenses.getCategory()
                   ))
                   .toList();


    }

}
