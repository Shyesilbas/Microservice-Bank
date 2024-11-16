package com.serhat.bank.kafka;

import com.serhat.bank.client.CustomerClient;
import com.serhat.bank.client.CustomerResponse;
import com.serhat.bank.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableAsync
public class AccountKafkaListener {

    private final CustomerClient customerClient;
    @Async
    @KafkaListener(topics = "Account-created",groupId = "Account")
    public void accountCreated(AccountCreatedEvent event){
        log.info("Event received : " +event);
        CustomerResponse customer = customerClient.findCustomerById(event.customerId());
        if(customer != null){
            customerClient.updateRelatedAccount(String.valueOf(customer.id()),event.accountNumber());
            log.info("Successfully Updated the related Accounts for customer.");
        }else{
            log.warn("Customer Id Not found!");
        }
    }
}
