package com.serhat.expenses.kafka;

import com.serhat.expenses.client.CreditCardClient;
import com.serhat.expenses.dto.CreditCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class expensesKafkaListener {
    private final CreditCardClient creditCardClient;


    @KafkaListener(topics = "Payment-successful",groupId = "expense")
    public void paymentSuccessful(PaymentSuccessfulEvent event){
        log.info("Event Received : "+event);
        CreditCardResponse creditCardResponse = creditCardClient.findCardByCardNumber(event.cardNumber());
        BigDecimal debt = creditCardResponse.debt();
        BigDecimal updatedDebt = debt.add(event.amount());
        BigDecimal balance = creditCardResponse.balance();
        BigDecimal updatedBalance = balance.subtract(event.amount());
        if(creditCardResponse!= null){
            creditCardClient.updateDebtAndBalanceAfterProcess(event.cardNumber(),updatedDebt,updatedBalance);
            log.info("Credit Card debt and balance updated successfully");
        }else{
            log.warn("Credit Card Not Found");
        }
    }
}
