package com.serhat.bank.service;

import com.serhat.bank.dto.CustomerRequest;
import com.serhat.bank.dto.CustomerResponse;
import com.serhat.bank.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerMapper {



    public Customer mapToCustomer(CustomerRequest request){
        if(request == null){
            throw new IllegalArgumentException("Request cannot be null");
        }
        return Customer.builder()
                .personalId(request.personalId())
                .name(request.name())
                .surname(request.surname())
                .email(request.email())
                .monthlyIncome(request.monthlyIncome())
                .occupation(request.occupation())
                .build();
    }

    public CustomerResponse customerData(Customer customer){
        return new CustomerResponse(
                customer.getId(),
                customer.getPersonalId(),
                customer.getName(),
                customer.getSurname(),
                customer.getEmail(),
                customer.getMonthlyIncome(),
                customer.getOccupation()
        );
    }


}
