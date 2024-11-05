package com.serhat.creditcard.repository;

import com.serhat.creditcard.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard,Integer> {
    boolean existsByCardNumber(String cardNumber);

    boolean existsByCvv(String cvv);
}
