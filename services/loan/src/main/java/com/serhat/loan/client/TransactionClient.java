package com.serhat.loan.client;

import com.serhat.loan.dto.LoanInstallmentPayRequest;
import com.serhat.loan.dto.LoanInstallmentPaymentResponse;
import com.serhat.loan.dto.LoanRequest;
import com.serhat.loan.dto.LoanResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "transaction-service",url = "http://localhost:8050/api/v1/transactions")
public interface TransactionClient {

    @PostMapping("/loan")
    LoanResponse updateTransactionsAfterLoan(LoanRequest request);


    @PostMapping("/loanPayment")
    LoanInstallmentPaymentResponse updateTransactionAfterLoanInstallmentPayment(LoanInstallmentPayRequest request);
}
