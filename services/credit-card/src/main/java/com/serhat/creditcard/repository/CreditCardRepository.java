package com.serhat.creditcard.repository;

import com.serhat.creditcard.dto.CreditCardResponse;
import com.serhat.creditcard.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard,Integer> {
    boolean existsByCardNumber(String cardNumber);

    boolean existsByCvv(String cvv);

    Optional<CreditCard> findByCardNumber(String cardNumber);

}
