package com.serhat.transactions.repository;

import com.serhat.transactions.dto.TransactionHistory;
import com.serhat.transactions.entity.Transaction;
import com.serhat.transactions.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,String> {

    List<Transaction> findByReceiverAccountNumberAndTransactionType(String accountNumber, TransactionType type);


    @Query("SELECT t FROM Transaction t WHERE t.senderAccountNumber = :accountNumber OR t.receiverAccountNumber = :accountNumber")
    List<Transaction> findAllTransactions(String accountNumber);
}
