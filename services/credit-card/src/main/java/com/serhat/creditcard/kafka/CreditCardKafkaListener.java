package com.serhat.creditcard.kafka;
import com.serhat.creditcard.client.*;
import com.serhat.creditcard.entity.CreditCard;
import com.serhat.creditcard.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCardKafkaListener {
    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final TransactionClient transactionClient;

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

    @KafkaListener(topics = "CardDebtPayed", groupId = "card-service-group")
    public void listenerPayCardDebt(@Payload PayedCardDebtEvent event) {
        log.info("Received Pay Card Debt event: {}", event);

        try {
            Integer customerId = Integer.parseInt(event.customerId());
            String accountNumber = event.accountNumber();
            String cardNumber = event.cardNumber();
            String description = event.description();
            BigDecimal balance = event.balance();

            CustomerResponse customerResponse = customerClient.findCustomerById(customerId);
            if (customerResponse == null) {
                log.warn("Customer not found for ID: {}", customerId);
                return;
            }

            AccountResponse accountResponse = accountClient.findByAccountNumber(accountNumber);
            if (accountResponse == null) {
                log.warn("Account not found for Account Number: {}", accountNumber);
                return;
            }

            log.info("Successfully updated debt and balance for Credit Card: {}, with description: {}", cardNumber, description);
            accountClient.updateBalanceAfterCardDebtPayment(accountNumber, balance);
            transactionClient.updateTransactionHistoryAfterCardDebtPayment(event);

            log.info("Updated account balance for Account Number: {}", accountNumber);

        } catch (NumberFormatException e) {
            log.error("Error parsing customer ID: {}", event.customerId(), e);
        } catch (NoSuchElementException e) {
            log.error("Credit Card not found for Card Number: {}", event.cardNumber(), e);
        } catch (Exception e) {
            log.error("Error processing Pay Card Debt event", e);
        }


    }
}


