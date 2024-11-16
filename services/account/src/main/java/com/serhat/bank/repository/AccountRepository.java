package com.serhat.bank.repository;

import com.serhat.bank.model.Account;
import com.serhat.bank.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Integer> {
    boolean existsByAccountNumber(int accountNumber);

    List<Account> findByCustomerId(Integer customerId);

    Optional<Account> findByAccountNumber(int accountNumber);

    List<Account> findAccountByCurrencyAndCustomerId(Currency currency ,Integer customerId);
}
