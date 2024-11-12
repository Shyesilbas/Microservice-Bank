package com.serhat.loan.kafka;

import com.serhat.loan.client.AccountClient;
import com.serhat.loan.client.TransactionClient;
import com.serhat.loan.dto.AccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanKafkaListener {
    private final AccountClient accountClient;
    private final TransactionClient transactionClient;


    @KafkaListener(topics = "Loan-application",groupId = "loan")
    public void loanApplication(LoanApplicationEvent event){
        log.info("Event Received : "+event);
        AccountResponse accountResponse = accountClient.findByAccountNumber(event.accountNumber());
        if(accountResponse!= null){
            accountClient.updateBalanceAfterLoanApplication(event);
            transactionClient.updateTransactionsAfterLoan(event);
            log.info("Account Balance And Transaction history updated successfully");
        }else{
            log.warn("Account Not Found For number : "+event.accountNumber());
        }
    }
}
