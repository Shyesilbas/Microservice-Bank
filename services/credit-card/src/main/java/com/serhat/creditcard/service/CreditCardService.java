package com.serhat.creditcard.service;

import com.serhat.creditcard.client.*;
import com.serhat.creditcard.dto.CardDebtPaymentRequest;
import com.serhat.creditcard.dto.CardDebtPaymentResponse;
import com.serhat.creditcard.dto.CreditCardRequest;
import com.serhat.creditcard.dto.CreditCardResponse;
import com.serhat.creditcard.entity.CardStatus;
import com.serhat.creditcard.entity.CreditCard;
import com.serhat.creditcard.exception.AccountNotFoundException;
import com.serhat.creditcard.exception.CardNotFoundException;
import com.serhat.creditcard.exception.InsufficientBalanceException;
import com.serhat.creditcard.exception.PaymentRequestMismatchException;
import com.serhat.creditcard.kafka.CardCreatedEvent;
import com.serhat.creditcard.kafka.PayedCardDebtEvent;
import com.serhat.creditcard.kafka.Status;
import com.serhat.creditcard.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardService {
    private final CreditCardRepository repository;
    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final KafkaTemplate<String, CardCreatedEvent> kafkaTemplateForCardCreated;
    private final KafkaTemplate<String,PayedCardDebtEvent> payedCardDebtEventKafkaTemplate;
    public CreditCardResponse createCreditCard(CreditCardRequest request){
        CustomerResponse customerResponse = customerClient.findCustomerById(request.customerId());
        AccountResponse accountResponse = accountClient.findByAccountNumber(request.linkedAccountNumber());

        if(customerResponse == null){
            throw new RuntimeException("Customer Not found");
        }
        if(accountResponse == null){
            throw new RuntimeException("Account not found to Link the Card");
        }

        CreditCard creditCard = CreditCard.builder()
                .customerId(String.valueOf(request.customerId()))
                .cardType(request.cardType())
                .rewardPoints(0)
                .cvv(generateCvv())
                .cardNumber(generateCardNumber())
                .cardLimit(request.limit())
                .balance(request.limit())
                .debt(BigDecimal.ZERO)
                .cardStatus(CardStatus.ACTIVE)
                .paymentDay(request.paymentDay())
                .billSending(request.billSending())
                .linkedAccountNumber(request.linkedAccountNumber())
                .build();

        repository.save(creditCard);
       // customerClient.updateLinkedCreditCards(customerResponse.id(),creditCard.getId());
      //  accountClient.updateLinkedCreditCards(accountResponse.id(),creditCard.getId());
        CardCreatedEvent cardCreatedEvent = new CardCreatedEvent(
                creditCard.getId(),
                Status.SUCCESSFUL,
                customerResponse.id(),
                request.linkedAccountNumber(),
                creditCard.getCardNumber(),
                request.limit(),
                request.cardType()
        );
        log.info("Kafka Topic Sending For Card-created Topic - Started");
        kafkaTemplateForCardCreated.send("Card-created",cardCreatedEvent);
        log.info("Kafka Topic Sent Successfully to topic Card-created -END");



        return new CreditCardResponse(
                customerResponse.id(),
                customerResponse.name(),
                customerResponse.surname(),
                request.limit(),
                request.paymentDay(),
                request.cardType(),
                request.billSending(),
                creditCard.getBalance(),
                creditCard.getCardNumber(),
                creditCard.getDebt()
        );
    }

    private String generateCardNumber() {
        Random random = new Random();
        String cardNumber;
        do {

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            cardNumber = sb.toString();
        } while (repository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    private String generateCvv(){
        Random random = new Random();
        String cvv;
        do {

            cvv = String.format("%03d", random.nextInt(1000));
        } while (repository.existsByCvv(cvv));
        return cvv;
    }

    public CreditCardResponse findCardByCardNumber(String cardNumber) {

        CreditCard creditCard = repository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card Not found with card number: " + cardNumber));

        // Fetch the customer details associated with the card
        CustomerResponse customerResponse = customerClient.findCustomerById(Integer.valueOf(creditCard.getCustomerId()));

        // Map the credit card and customer details to a response DTO
        return new CreditCardResponse(
                customerResponse.id(),
                customerResponse.name(),
                customerResponse.surname(),
                creditCard.getCardLimit(),
                creditCard.getPaymentDay(),
                creditCard.getCardType(),
                creditCard.getBillSending(),
                creditCard.getBalance(),
                creditCard.getCardNumber(),
                creditCard.getDebt()
        );
    }

    @Transactional
    public void updateDebtAndBalanceAfterProcess(String cardNumber, BigDecimal updatedDebt, BigDecimal updatedBalance) {
        CreditCard creditCard = repository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Credit Card not found with card number: " + cardNumber));

        creditCard.setDebt(updatedDebt);
        creditCard.setBalance(updatedBalance);

        repository.save(creditCard);
        log.info("Updated debt and balance for card number {}: Debt = {}, Balance = {}", cardNumber, updatedDebt, updatedBalance);
    }

    @Transactional
    public CardDebtPaymentResponse payCardDebt(CardDebtPaymentRequest request) {

        CreditCard creditCard = repository.findByCardNumber(request.cardNumber())
                .orElseThrow();

        AccountResponse accountResponse = accountClient.findByAccountNumber(request.accountNumber());
        CustomerResponse customerResponse = customerClient.findCustomerById(request.customerId());

        if (accountResponse == null) {
            throw new AccountNotFoundException("Account Not Found.");
        }
        if(request.amount().compareTo(accountResponse.balance())>0){
            throw new InsufficientBalanceException("Insufficient Balance At Account!");
        }

        BigDecimal debt = creditCard.getDebt();
        System.out.println("Total Card Debt : "+debt);
        BigDecimal balance = creditCard.getBalance();
        System.out.println("Total Card Balance : "+balance);


        if (request.amount().compareTo(debt) > 0) {
            throw new PaymentRequestMismatchException("Payment Amount cannot be higher than the total debt .");
        }

        BigDecimal updatedDebt = debt.subtract(request.amount());
        System.out.println("Updated Card Debt : "+updatedDebt);
        BigDecimal updatedBalance = balance.add(request.amount());
        System.out.println("Updated Card Balance : "+updatedBalance);
        creditCard.setDebt(updatedDebt);
        creditCard.setBalance(updatedBalance);
        repository.save(creditCard);


        BigDecimal accountBalance = accountResponse.balance();
        System.out.println("Account Balance : "+accountBalance);
        BigDecimal updatedAccountBalance = accountResponse.balance().subtract(request.amount());
        System.out.println("Updated Account Balance After Card debt payment: "+updatedAccountBalance);
        PayedCardDebtEvent payedCardDebtEvent = new PayedCardDebtEvent(
                customerResponse.id(),
                request.accountNumber(),
                request.cardNumber(),
                request.description(),
                request.amount(),
                updatedDebt,
                updatedBalance
        );
        log.info("Kafka Topic Sending For PayCardDebtTopic - Started");
        payedCardDebtEventKafkaTemplate.send("PayCardDebt",payedCardDebtEvent);
        log.info("Kafka Topic Sent Successfully to topic PayCardDebt -END");

        return new CardDebtPaymentResponse(
                customerResponse.id(),
                request.accountNumber(),
                request.cardNumber(),
                request.description(),
                request.amount(),
                updatedDebt,
                updatedBalance
        );
    }




}
