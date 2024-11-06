package com.serhat.creditcard.service;

import com.serhat.creditcard.client.AccountClient;
import com.serhat.creditcard.client.AccountResponse;
import com.serhat.creditcard.client.CustomerClient;
import com.serhat.creditcard.client.CustomerResponse;
import com.serhat.creditcard.dto.CreditCardRequest;
import com.serhat.creditcard.dto.CreditCardResponse;
import com.serhat.creditcard.entity.CardStatus;
import com.serhat.creditcard.entity.CreditCard;
import com.serhat.creditcard.kafka.CardCreatedEvent;
import com.serhat.creditcard.kafka.Status;
import com.serhat.creditcard.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditCardService {
    private final CreditCardRepository repository;
    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final KafkaTemplate<String, CardCreatedEvent> kafkaTemplateForCardCreated;
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
        customerClient.updateLinkedCreditCards(customerResponse.id(),creditCard.getId());
        accountClient.updateLinkedCreditCards(accountResponse.id(),creditCard.getId());
        CardCreatedEvent cardCreatedEvent = new CardCreatedEvent(creditCard.getId(), Status.SUCCESSFUL);
        log.info("Kafka Topic Sending For Card-created Topic - Started");
        kafkaTemplateForCardCreated.send("Card-created",cardCreatedEvent);
        log.info("Kafka Topic Sent Successfully to topic Card-created -END");



        return new CreditCardResponse(
                customerResponse.name(),
                customerResponse.surname(),
                request.limit(),
                request.paymentDay(),
                request.cardType(),
                request.billSending(),
                creditCard.getBalance(),
                creditCard.getCardNumber()
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
        // Fetch the credit card by card number
        CreditCard creditCard = repository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card Not found with card number: " + cardNumber));

        // Fetch the customer details associated with the card
        CustomerResponse customerResponse = customerClient.findCustomerById(Integer.valueOf(creditCard.getCustomerId()));

        // Map the credit card and customer details to a response DTO
        return new CreditCardResponse(
                customerResponse.name(),
                customerResponse.surname(),
                creditCard.getCardLimit(),
                creditCard.getPaymentDay(),
                creditCard.getCardType(),
                creditCard.getBillSending(),
                creditCard.getBalance(),
                creditCard.getCardNumber()
        );
    }

    public void updateDebtAndBalanceAfterProcess(String cardNumber, BigDecimal updatedDebt, BigDecimal updatedBalance) {
        CreditCard creditCard = repository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Credit Card not found with card number: " + cardNumber));

        creditCard.setDebt(updatedDebt);
        creditCard.setBalance(updatedBalance);

        repository.save(creditCard);
        log.info("Updated debt and balance for card number {}: Debt = {}, Balance = {}", cardNumber, updatedDebt, updatedBalance);
    }

}
