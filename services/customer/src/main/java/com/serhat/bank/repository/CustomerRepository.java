package com.serhat.bank.repository;

import com.serhat.bank.dto.CustomerResponse;
import com.serhat.bank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Integer> {
    Optional<CustomerResponse> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPersonalId(String personalId);

    Optional<Customer> findByPersonalId(String personalId);
}
