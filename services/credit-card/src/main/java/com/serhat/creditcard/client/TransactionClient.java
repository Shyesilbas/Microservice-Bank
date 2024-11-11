package com.serhat.creditcard.client;

import com.serhat.creditcard.dto.CardDebtPaymentRequest;
import com.serhat.creditcard.dto.CardDebtPaymentResponse;
import com.serhat.creditcard.kafka.PayedCardDebtEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service",url = "http://localhost:8050/api/v1/transactions")
public interface TransactionClient {

    @PostMapping("/cardDebtPayment")
    CardDebtPaymentResponse updateTransactionHistoryAfterCardDebtPayment(@RequestBody CardDebtPaymentRequest request);

    @PostMapping("/cardDebtPayment")
    void updateTransactionHistoryAfterCardDebtPayment(@RequestBody PayedCardDebtEvent event);
}
