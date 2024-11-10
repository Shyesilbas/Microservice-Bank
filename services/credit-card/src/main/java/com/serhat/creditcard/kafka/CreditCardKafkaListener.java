package com.serhat.creditcard.kafka;
import com.serhat.creditcard.client.AccountClient;
import com.serhat.creditcard.client.AccountResponse;
import com.serhat.creditcard.client.CustomerClient;
import com.serhat.creditcard.client.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCardKafkaListener {
    private final CustomerClient customerClient;
    private final AccountClient accountClient;

    @KafkaListener(topics = "Card-created", groupId = "card-service-group")
    public void listenerCardCreated(
           @Payload CardCreatedEvent event) {
        log.info("Received CardCreatedEvent: {}", event);

        try {
            Integer customerId = Integer.parseInt(String.valueOf(event.customerId()));
            CustomerResponse customerResponse = customerClient.findCustomerById(customerId);

            AccountResponse accountResponse = accountClient.findByAccountNumber(event.linkedAccountNumber());

            if (customerResponse != null) {
                customerClient.updateLinkedCreditCards(customerResponse.id(), event.cardId());
            } else {
                log.warn("Customer not found for ID: {}", event.customerId());
            }

            if (accountResponse != null) {
                accountClient.updateLinkedCreditCards(accountResponse.id(), event.cardId());
            } else {
                log.warn("Account not found for Account Number: {}", event.linkedAccountNumber());
            }

            log.info("Successfully updated linked credit card for customer and account.");
        } catch (NumberFormatException e) {
            log.error("Error parsing customer ID: {}", event.customerId(), e);
        } catch (Exception e) {
            log.error("Error updating linked credit card", e);
        }
    }

}


