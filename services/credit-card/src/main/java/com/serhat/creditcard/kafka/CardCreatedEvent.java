package com.serhat.creditcard.kafka;

import com.serhat.creditcard.entity.CardType;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

public record CardCreatedEvent(
         Integer cardId,
         Status status,
         String customerId,
         String linkedAccountNumber,
         String cardNumber,
         BigDecimal limit,
         CardType cardType
) {
}
