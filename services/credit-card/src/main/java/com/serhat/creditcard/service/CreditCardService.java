package com.serhat.creditcard.service;

import com.serhat.creditcard.client.AccountClient;
import com.serhat.creditcard.client.AccountResponse;
import com.serhat.creditcard.client.CustomerClient;
import com.serhat.creditcard.client.CustomerResponse;
import com.serhat.creditcard.dto.CreditCardRequest;
import com.serhat.creditcard.dto.CreditCardResponse;
import com.serhat.creditcard.entity.CardStatus;
import com.serhat.creditcard.entity.CreditCard;
import com.serhat.creditcard.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CreditCardService {
    private final CreditCardRepository repository;
    private final CustomerClient customerClient;
    private final AccountClient accountClient;
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
        return new CreditCardResponse(
                customerResponse.name(),customerResponse.surname(),request.limit(),request.paymentDay(),request.cardType(),request.billSending()
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


}
