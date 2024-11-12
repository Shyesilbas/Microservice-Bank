package com.serhat.loan.client;

import com.serhat.loan.dto.*;
import com.serhat.loan.kafka.LoanApplicationEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service",url = "http://localhost:8050/api/v1/transactions")
public interface TransactionClient {

    @PostMapping("/loan")
    LoanResponse updateTransactionsAfterLoan(@RequestBody LoanRequest request);

    @PostMapping("/loan")
    LoanApplicationEvent updateTransactionsAfterLoan(@RequestBody LoanApplicationEvent event);


    @PostMapping("/loanInstallmentPayment")
    LoanInstallmentPaymentResponse updateTransactionAfterLoanInstallmentPayment(@RequestBody LoanInstallmentPayRequest request);

    @PostMapping("/loanTotalPayment")
    payTotalLoanDebtResponse updateTransactionAfterLoanTotalDebtPayment(@RequestBody payTotalLoanDebtRequest request);



}
