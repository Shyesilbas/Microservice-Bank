package com.serhat.creditcard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "creditCard")
@EntityListeners(AuditingEntityListener.class)
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private CardType cardType;
    private String cardNumber;
    private Integer rewardPoints;
    @CreatedDate
    private LocalDateTime createdAt;
    private String cvv;
    private String customerId;
    private BigDecimal cardLimit;
    private BigDecimal balance;
    private BigDecimal debt;
    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;
    private LocalDate expirationDate;
    private PaymentDay paymentDay;
    @Enumerated(EnumType.STRING)
    private BillSending billSending;
    private String linkedAccountNumber;

    @PrePersist
    public void setExpirationDate() {
        if (expirationDate == null && createdAt != null) {
            expirationDate = createdAt.toLocalDate().plusYears(5);
        }
    }


}
