package com.serhat.bank.controller;

import com.serhat.bank.dto.CustomerRequest;
import com.serhat.bank.dto.CustomerResponse;
import com.serhat.bank.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService service;

    @PostMapping("/saveCustomer")
    public ResponseEntity<String> createCustomer(@RequestBody CustomerRequest request){
        return ResponseEntity.ok(service.createCustomer(request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll(){
        return ResponseEntity.ok(service.allCustomers());
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Integer customerId){
        return ResponseEntity.ok(service.deleteCustomer(customerId));
    }

}
