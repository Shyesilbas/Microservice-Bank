package com.serhat.bank.service;

import com.serhat.bank.dto.CustomerRequest;
import com.serhat.bank.dto.CustomerResponse;
import com.serhat.bank.model.Customer;
import com.serhat.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public String createCustomer(CustomerRequest request){
        if (checkEmailExists(request.email())) {
            throw new IllegalArgumentException("Email is already in usage");
        }
        if (request.email().isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (checkPersonalIdExists(request.personalId())) {
            throw new IllegalArgumentException("Customer Found for the personal Id");
        }
        if (request.personalId().isBlank()) {
            throw new IllegalArgumentException("Personal Id is required");
        }

     Customer customer = mapper.mapToCustomer(request);
     Customer savedCustomer = repository.save(customer);

     return "Customer created Successfully! Name : "+request.name()+" Personal Id : "+request.personalId() + " Customer Id : "+savedCustomer.getId();
    }

    public List<CustomerResponse> allCustomers(){
        return this.repository.findAll()
                .stream()
                .map(mapper::customerData)
                .toList();
    }

    public boolean checkEmailExists(String email){
        return this.repository.existsByEmail(email);
    }

    public boolean checkPersonalIdExists(String personalId){
        return this.repository.existsByPersonalId(personalId);
    }

    public boolean checkIdExists(Integer id){
        return this.repository.existsById(id);
    }

    public CustomerResponse findByCustomerId(Integer customerId) {
        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer Not found"));
        return mapper.customerData(customer);
    }

    public String deleteCustomer(Integer id){
        if(checkIdExists(id)) {
            this.repository.deleteById(id);
            return "Customer with id : " + id + " deleted Successfully";
        }
        throw new RuntimeException("Customer Not found for Id : "+id);
    }

}
