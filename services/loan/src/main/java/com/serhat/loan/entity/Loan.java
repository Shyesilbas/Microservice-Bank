package com.serhat.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String accountNumber;
    private String customerId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private String description;
    private Integer installment;
    private Integer paymentDay;
    private BigDecimal payback;
    private BigDecimal debtLeft;
    @CreatedDate
    private LocalDateTime loanApplicationDate;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;
    @Enumerated(EnumType.STRING)
    private LoanType loanType;



}
