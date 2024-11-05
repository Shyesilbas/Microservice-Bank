package com.serhat.creditcard.dto;

import com.serhat.creditcard.entity.BillSending;
import com.serhat.creditcard.entity.CardType;
import com.serhat.creditcard.entity.PaymentDay;

import java.math.BigDecimal;

public record CreditCardRequest(
        Integer customerId,
        CardType cardType,
        BigDecimal limit,
        PaymentDay paymentDay,
        BillSending billSending ,
        String linkedAccountNumber
) {
}
