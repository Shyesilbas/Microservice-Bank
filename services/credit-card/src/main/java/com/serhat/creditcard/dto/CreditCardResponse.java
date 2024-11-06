package com.serhat.creditcard.dto;

import com.serhat.creditcard.entity.BillSending;
import com.serhat.creditcard.entity.CardType;
import com.serhat.creditcard.entity.PaymentDay;

import java.math.BigDecimal;

public record CreditCardResponse(
        String customerName,
        String customerSurname,
        BigDecimal limit,
        PaymentDay paymentDay,
        CardType cardType,
        BillSending billSending,
        BigDecimal balance,
        String cardNumber
) {
}
