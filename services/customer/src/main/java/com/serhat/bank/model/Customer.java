package com.serhat.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "personalId",nullable = false,unique = true)
    private String personalId;
    @Column(name = "name",nullable = false,unique = false)
    private String name;
    @Column(name = "surname",nullable = false,unique = false)
    private String surname;
    @Column(name = "email",nullable = false,unique = true)
    private String email;
    @Column(name = "monthly_income",nullable = false,unique = false)
    private BigDecimal monthlyIncome;
    @Enumerated(EnumType.STRING)
    private Occupation occupation;


}
