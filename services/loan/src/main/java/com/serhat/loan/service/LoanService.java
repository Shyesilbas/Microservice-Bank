package com.serhat.loan.service;

import com.serhat.loan.Repository.LoanRepository;
import com.serhat.loan.client.AccountClient;
import com.serhat.loan.client.TransactionClient;
import com.serhat.loan.dto.*;
import com.serhat.loan.entity.ApplicationStatus;
import com.serhat.loan.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository repository;
    private final AccountClient accountClient;
    private final TransactionClient transactionClient;


    @Transactional
    public LoanResponse applyToLoan(LoanRequest request) {
        AccountResponse accountResponse = accountClient.findByAccountNumber(request.accountNumber());
        if (accountResponse == null) {
            throw new RuntimeException("Account not found: " + request.accountNumber());
        }

        BigDecimal monthlyInterestRate = new BigDecimal("0.05");
        BigDecimal principal = request.amount();
        Integer installments = request.installment();

        BigDecimal payback = principal
                .multiply(monthlyInterestRate.add(BigDecimal.ONE).pow(installments))
                .setScale(2, RoundingMode.HALF_UP);


        Loan loan = Loan.builder()
                .accountNumber(request.accountNumber())
                .customerId(request.customerId())
                .amount(request.amount())
                .interestRate(monthlyInterestRate)
                .description(request.description())
                .installment(request.installment())
                .paymentDay(request.paymentDay())
                .payback(payback)
                .loanApplicationDate(LocalDateTime.now())
                .applicationStatus(ApplicationStatus.APPROVED)
                .loanType(request.loanType())
                .build();

        accountClient.updateBalanceAfterLoanApplication(request);
        transactionClient.updateTransactionsAfterLoan(request);
        repository.save(loan);

        return new LoanResponse(
                request.customerId(),
                request.amount(),
                request.accountNumber(),
                request.installment(),
                request.description(),
                payback,
                request.loanType(),
                request.paymentDay(),
                ApplicationStatus.PENDING
        );
    }

    public LoanResponse findByLoanId(Integer loanId){
        Loan loan = repository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found for id: " + loanId));

        return new LoanResponse(
                loan.getCustomerId(),
                loan.getAmount(),
                loan.getAccountNumber(),
                loan.getInstallment(),
                loan.getDescription(),
                loan.getPayback(),
                loan.getLoanType(),
                loan.getPaymentDay(),
                loan.getApplicationStatus()
        );
    }
// todo CHECK DEBT LEFT PROCESSES

    @Transactional
   public LoanInstallmentPaymentResponse payLoanInstallment(LoanInstallmentPayRequest request){
       AccountResponse accountResponse = accountClient.findByAccountNumber(request.accountNumber());
       if (accountResponse == null) {
           throw new RuntimeException("Account not found: " + request.accountNumber());
       }

       Loan loan = repository.findById(request.loanId())
               .orElseThrow();

       BigDecimal totalDebt = loan.getDebtLeft();
       BigDecimal debtLeft = totalDebt.subtract(request.amount());

       if (accountResponse.balance().compareTo(request.amount()) < 0) {
           throw new RuntimeException("Insufficient balance for loan installment payment.");
       }
       accountClient.payLoanInstallment(request);
       transactionClient.updateTransactionAfterLoanInstallmentPayment(request);

       return new LoanInstallmentPaymentResponse(
               request.amount(),
               request.accountNumber(),
               debtLeft
       );
   }


}
