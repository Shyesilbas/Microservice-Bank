package com.serhat.loan.service;

import com.serhat.loan.Repository.LoanRepository;
import com.serhat.loan.client.AccountClient;
import com.serhat.loan.client.TransactionClient;
import com.serhat.loan.dto.*;
import com.serhat.loan.entity.ApplicationStatus;
import com.serhat.loan.entity.Loan;
import com.serhat.loan.exception.*;
import com.serhat.loan.kafka.LoanApplicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {
    private final LoanRepository repository;
    private final AccountClient accountClient;
    private final TransactionClient transactionClient;
    private final KafkaTemplate<String, LoanApplicationEvent> loanApplicationEventKafkaTemplate;

    @Transactional
    public LoanResponse applyToLoan(LoanRequest request) {
        AccountResponse accountResponse = accountClient.findByAccountNumber(request.accountNumber());
        if (accountResponse == null) {
            throw new AccountNotFoundException("Account not found: " + request.accountNumber());
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

        repository.save(loan);
        log.info("Kafka Message Sending to Topic Loan-application   -> STARTED");
        LoanApplicationEvent loanApplicationEvent = new LoanApplicationEvent(
                request.customerId(),
                request.amount(),
                request.accountNumber(),
                request.installment(),
                request.description(),
                request.loanType(),
                request.paymentDay()
        );
        loanApplicationEventKafkaTemplate.send("Loan-application",loanApplicationEvent);
        log.info("Kafka Message Send successfully to topic Loan-application -> END");

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
                .orElseThrow(() -> new LoanNotFoundException("Loan not found for id: " + loanId));

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
            throw new AccountNotFoundException("Account not found: " + request.accountNumber());
        }

        Loan loan = repository.findById(request.loanId())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + request.loanId()));

        BigDecimal monthlyPayment = loan.getMonthlyPayment();

        if (request.amount().compareTo(monthlyPayment) != 0) {
            throw new MonthlyPaymentMismatchException("Monthly Payment is: " + monthlyPayment + ", not: " + request.amount());
        }
        if (loan.getInstallmentLeft() <= 0) {
            throw new DebtAlreadyPaidException("Loan debt is fully paid!");
        }

        if (accountResponse.balance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for loan installment payment.");
        }

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
                .orElseThrow(() -> new LoanNotFoundException("Loan not found for id: " + loanId));

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
            throw new AccountNotFoundException("Account not found: " + request.accountNumber());
        }
        Loan loan = repository.findById(request.loanId())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + request.loanId()));


        BigDecimal accountBalance = accountResponse.balance();
        System.out.println("Account Balance : "+accountBalance);
        BigDecimal totalDebtLeft = loan.getDebtLeft();
        System.out.println("Total Debt left : "+totalDebtLeft);
        Integer totalInstallmentLeft = loan.getInstallmentLeft();

        if (totalInstallmentLeft <= 0) {
            throw new DebtAlreadyPaidException("Loan debt is fully paid!");
        }
        if (request.amount().compareTo(totalDebtLeft) != 0) {
            throw new TotalLoanPaymentMismatchException("Total Payment is: " + totalDebtLeft + ", not: " + request.amount());
        }
        if (accountResponse.balance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for loan installment payment.");
        }

        loan.setInstallmentLeft(0);
        loan.setDebtLeft(new BigDecimal(0));
        repository.save(loan);
        accountClient.payTotalLoanDebt(request);
        transactionClient.updateTransactionAfterLoanTotalDebtPayment(request);
        System.out.println("Account Balance After payment : "+accountBalance.subtract(request.amount()));
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
