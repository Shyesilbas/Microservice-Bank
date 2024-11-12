package com.serhat.bank.service;



import com.serhat.bank.client.AccountClient;
import com.serhat.bank.client.AccountResponse;
import com.serhat.bank.dto.CustomerRequest;
import com.serhat.bank.dto.CustomerResponse;
import com.serhat.bank.exception.CustomerHasNoAccountsException;
import com.serhat.bank.exception.CustomerNotFoundException;
import com.serhat.bank.kafka.CustomerCreatedEvent;
import com.serhat.bank.kafka.Status;
import com.serhat.bank.model.Customer;
import com.serhat.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository repository;
    private final AccountClient accountClient;
    private final CustomerMapper mapper;
    private final KafkaTemplate<String, CustomerCreatedEvent> kafkaTemplate;

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
     CustomerCreatedEvent customerCreatedEvent = new CustomerCreatedEvent(request.personalId(), Status.CREATED);
     log.info("Kafka Message Start");
     kafkaTemplate.send("Customer-created",customerCreatedEvent);
     log.info("Kafka Message End");


     return "Customer created Successfully! Name : "+request.name()+" Personal Id : "+request.personalId() + " Customer Id : "+savedCustomer.getId();
    }

    public void updateLinkedCreditCards(Integer customerId) {

        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));


        customer.setRelatedCreditCard(customer.getRelatedCreditCard() + 1);
        repository.save(customer);
    }

    public void updateRelatedAccount(Integer customerId) {

        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));


        customer.setRelatedAccount(customer.getRelatedAccount() + 1);
        repository.save(customer);
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

    public ResponseEntity<CustomerResponse> findByCustomerId(Integer customerId) {
        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found For id : "+customerId));
        return ResponseEntity.ok(mapper.customerData(customer));
    }

    public String deleteCustomer(Integer id){
        if(checkIdExists(id)) {
            this.repository.deleteById(id);
            return "Customer with id : " + id + " deleted Successfully";
        }
        throw new RuntimeException("Customer Not found for Id : "+id);
    }

    public ResponseEntity<List<AccountResponse>> findAccountsByCustomerId(Integer customerId) {
        List<AccountResponse> accounts = accountClient.findAccountsByCustomerId(customerId);

        if (accounts == null || accounts.isEmpty()) {
            throw new CustomerHasNoAccountsException("Customer with ID " + customerId + " has no accounts.");
        }
        return ResponseEntity.ok(accounts);
    }


}
