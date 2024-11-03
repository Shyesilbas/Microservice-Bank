package com.serhat.bank.repository;

import com.serhat.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account,Integer> {
    boolean existsByAccountNumber(int accountNumber);

    List<Account> findByCustomerId(Integer customerId);
}
