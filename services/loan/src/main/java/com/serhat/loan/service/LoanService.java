package com.serhat.loan.service;

import com.serhat.loan.Repository.LoanRepository;
import com.serhat.loan.client.AccountClient;
import com.serhat.loan.client.CustomerClient;
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
    private final CustomerClient customerClient;


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
        BigDecimal monthlyPayment = payback.divide(BigDecimal.valueOf(installments), RoundingMode.HALF_UP);


        Loan loan = Loan.builder()
                .accountNumber(request.accountNumber())
                .customerId(request.customerId())
                .amount(request.amount())
                .interestRate(monthlyInterestRate)
                .monthlyPayment(monthlyPayment)
                .installmentLeft(request.installment())
                .debtLeft(payback)
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
                monthlyPayment,
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
                loan.getMonthlyPayment(),
                loan.getPaymentDay(),
                loan.getApplicationStatus()
        );
    }


    @Transactional
    public LoanInstallmentPaymentResponse payLoanInstallment(LoanInstallmentPayRequest request) {
        AccountResponse accountResponse = accountClient.findByAccountNumber(request.accountNumber());
        if (accountResponse == null) {
            throw new RuntimeException("Account not found: " + request.accountNumber());
        }

        Loan loan = repository.findById(request.loanId())
                .orElseThrow(() -> new RuntimeException("Loan not found: " + request.loanId()));

        BigDecimal monthlyPayment = loan.getMonthlyPayment();

        if (request.amount().compareTo(monthlyPayment) < 0) {
            throw new RuntimeException("Monthly Payment is: " + monthlyPayment + ", not: " + request.amount());
        }
        if (loan.getInstallmentLeft() <= 0) {
            throw new RuntimeException("Loan debt is fully paid!");
        }

        if (accountResponse.balance().compareTo(request.amount()) < 0) {
            throw new RuntimeException("Insufficient balance for loan installment payment.");
        }

        // Deduct from debt and update installments
        BigDecimal totalDebt = loan.getDebtLeft();
        BigDecimal debtLeft = totalDebt.subtract(request.amount());
        loan.setDebtLeft(debtLeft);
        loan.setInstallmentLeft(loan.getInstallmentLeft() - 1);
        repository.save(loan);

        accountClient.payLoanInstallment(request);
        transactionClient.updateTransactionAfterLoanInstallmentPayment(request);

        return new LoanInstallmentPaymentResponse(
                request.amount(),
                request.accountNumber(),
                debtLeft
        );
    }

    public LoanResponseForTotalPayment findLoanByLoanId(Integer loanId) {
        Loan loan = repository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found for id: " + loanId));

        return new LoanResponseForTotalPayment(
                loan.getCustomerId(),
                loan.getAmount(),
                loan.getAccountNumber(),
                loan.getDebtLeft(),
                loan.getInstallment(),
                loan.getDescription(),
                loan.getPayback(),
                loan.getLoanType(),
                loan.getPaymentDay()
        );
    }
    @Transactional
    public payTotalLoanDebtResponse closeLoan(payTotalLoanDebtRequest request){

        AccountResponse accountResponse = accountClient.findByAccountNumber(request.accountNumber());
        if (accountResponse == null) {
            throw new RuntimeException("Account not found: " + request.accountNumber());
        }
        Loan loan = repository.findById(request.loanId())
                .orElseThrow(() -> new RuntimeException("Loan not found: " + request.loanId()));


        BigDecimal totalDebtLeft = loan.getDebtLeft();
        System.out.println("Total Debt left : "+totalDebtLeft);
        Integer totalInstallmentLeft = loan.getInstallmentLeft();

        if (totalInstallmentLeft <= 0) {
            throw new RuntimeException("Loan debt is fully paid!");
        }
        if (request.amount().compareTo(totalDebtLeft) != 0) {
            throw new RuntimeException("Total Payment is: " + totalDebtLeft + ", not: " + request.amount());
        }
        if (accountResponse.balance().compareTo(request.amount()) < 0) {
            throw new RuntimeException("Insufficient balance for loan installment payment.");
        }

        loan.setInstallmentLeft(0);
        loan.setDebtLeft(new BigDecimal(0));
        repository.save(loan);
        accountClient.payTotalLoanDebt(request);
        transactionClient.updateTransactionAfterLoanTotalDebtPayment(request);

        return new payTotalLoanDebtResponse(
                request.loanId(),
                request.accountNumber(),
                loan.getAmount(),
                loan.getPayback(),
                request.amount(),
                LoanStatus.FULLY_PAID
        );
    }
}
