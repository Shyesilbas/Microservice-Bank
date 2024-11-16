package com.serhat.bank.service;



import com.serhat.bank.client.AccountClient;
import com.serhat.bank.client.AccountResponse;
import com.serhat.bank.client.Currency;
import com.serhat.bank.dto.CustomerRequest;
import com.serhat.bank.dto.CustomerResponse;
import com.serhat.bank.exception.CustomerHasNoAccountsException;
import com.serhat.bank.exception.CustomerNotFoundException;
import com.serhat.bank.kafka.CustomerCreatedEvent;
import com.serhat.bank.kafka.Status;
import com.serhat.bank.model.Customer;
import com.serhat.bank.repository.CustomerRepository;
import feign.FeignException;
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

    public String createCustomer(CustomerRequest request) {
        try {
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

            // Kafka message send
            CustomerCreatedEvent customerCreatedEvent = new CustomerCreatedEvent(request.personalId(), Status.CREATED);
            log.info("Kafka Message Start");
            kafkaTemplate.send("Customer-created", customerCreatedEvent);
            log.info("Kafka Message End");

            return "Customer created Successfully! Name : " + request.name() + " Personal Id : " + request.personalId() + " Customer Id : " + savedCustomer.getId();
        } catch (Exception e) {
            log.error("Error creating customer", e);
            throw new RuntimeException("An error occurred while creating the customer: " + e.getMessage());
        }
    }

    public void updateLinkedCreditCards(Integer customerId) {
        try {
            Customer customer = repository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            customer.setRelatedCreditCard(customer.getRelatedCreditCard() + 1);
            repository.save(customer);
        } catch (FeignException e) {
            log.error("Feign exception occurred while updating linked credit card for customerId: {}", customerId, e);
            throw new RuntimeException("Error updating linked credit cards for customerId: " + customerId);
        } catch (Exception e) {
            log.error("Error updating linked credit card", e);
            throw new RuntimeException("An error occurred while updating the linked credit card for customerId: " + customerId);
        }
    }

    public void updateRelatedAccount(Integer customerId) {
        try {
            Customer customer = repository.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

            customer.setRelatedAccount(customer.getRelatedAccount() + 1);
            repository.save(customer);
        } catch (FeignException e) {
            log.error("Feign exception occurred while updating related account for customerId: {}", customerId, e);
            throw new RuntimeException("Error updating related account for customerId: " + customerId);
        } catch (Exception e) {
            log.error("Error updating related account", e);
            throw new RuntimeException("An error occurred while updating the related account for customerId: " + customerId);
        }
    }

    public List<CustomerResponse> allCustomers() {
        try {
            return this.repository.findAll()
                    .stream()
                    .map(mapper::customerData)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching all customers", e);
            throw new RuntimeException("An error occurred while fetching all customers: " + e.getMessage());
        }
    }

    public boolean checkEmailExists(String email) {
        return this.repository.existsByEmail(email);
    }

    public boolean checkPersonalIdExists(String personalId) {
        return this.repository.existsByPersonalId(personalId);
    }

    public boolean checkIdExists(Integer id) {
        return this.repository.existsById(id);
    }

    public CustomerResponse findByCustomerId(Integer customerId) {
        try {
            Customer customer = repository.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

            return new CustomerResponse(
                    customer.getId(),
                    customer.getPersonalId(),
                    customer.getName(),
                    customer.getSurname(),
                    customer.getEmail(),
                    customer.getMonthlyIncome(),
                    customer.getOccupation()
            );
        } catch (FeignException e) {
            log.error("Feign exception occurred while fetching customer by ID: {}", customerId, e);
            throw new CustomerNotFoundException("Error fetching customer with ID: " + customerId);
        } catch (CustomerNotFoundException e) {
            log.warn("Customer not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching customer by ID: {}", customerId, e);
            throw new RuntimeException("An unexpected error occurred while fetching the customer by ID: " + customerId, e);
        }
    }

    public String deleteCustomer(Integer id) {
        try {
            if (checkIdExists(id)) {
                this.repository.deleteById(id);
                return "Customer with id : " + id + " deleted Successfully";
            } else {
                throw new CustomerNotFoundException("Customer Not found for Id : " + id);
            }
        } catch (FeignException e) {
            log.error("Feign exception occurred while deleting customer with ID: {}", id, e);
            throw new CustomerNotFoundException("Error deleting customer with ID: " + id);
        } catch (Exception e) {
            log.error("Error deleting customer", e);
            throw new RuntimeException("An error occurred while deleting the customer with ID: " + id);
        }
    }

    public ResponseEntity<List<AccountResponse>> findAccountsByCustomerId(Integer customerId) {
        try {
            List<AccountResponse> accounts = accountClient.findAccountsByCustomerId(customerId);

            if (accounts == null || accounts.isEmpty()) {
                throw new CustomerHasNoAccountsException("Customer with ID " + customerId + " has no accounts.");
            }
            return ResponseEntity.ok(accounts);
        } catch (FeignException e) {
            log.error("Feign exception occurred while fetching accounts for customerId: {}", customerId, e);
            throw new RuntimeException("Error fetching accounts for customerId: " + customerId);
        } catch (Exception e) {
            log.error("Error fetching accounts", e);
            throw new RuntimeException("An error occurred while fetching accounts for customerId: " + customerId);
        }
    }

}
