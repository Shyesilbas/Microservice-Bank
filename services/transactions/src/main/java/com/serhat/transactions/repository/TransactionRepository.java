package com.serhat.transactions.repository;

import com.serhat.transactions.entity.Transaction;
import com.serhat.transactions.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,String> {

    List<Transaction> findByReceiverAccountNumberAndTransactionType(String accountNumber, TransactionType type);


}
