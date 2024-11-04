package com.serhat.transactions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;
    @Column(name = "senderCustomerId",nullable = true)
    private String senderCustomerId;
    @Column(name = "receiverCustomerId",nullable = true)
    private String receiverCustomerId;
    @Column(name = "senderAccount",nullable = true)
    private String senderAccountNumber;
    @Column(name = "receiverAccount",nullable = true)
    private String receiverAccountNumber;
    @Column(name = "description",nullable = true)
    private String description;
    private BigDecimal amount;
    @CreatedDate
    private LocalDateTime transactionDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;


}
