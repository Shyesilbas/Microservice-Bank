package com.serhat.bank.controller;

import com.serhat.bank.dto.CustomerRequest;
import com.serhat.bank.dto.CustomerResponse;
import com.serhat.bank.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService service;

    @Operation(summary = "Create a new Customer")
    @ApiResponse(responseCode = "200",description = "Customer Created Successfully")
    @PostMapping("/saveCustomer")
    public ResponseEntity<String> createCustomer(@RequestBody CustomerRequest request){
        return ResponseEntity.ok(service.createCustomer(request));
    }

    @Operation(summary = "Fetch the Customers")
    @ApiResponse(responseCode = "200",description = "Customers Fetched")
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll(){
        return ResponseEntity.ok(service.allCustomers());
    }

    @Operation(summary = "Delete a Customer")
    @ApiResponse(responseCode = "200",description = "Customer Deleted Successfully")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Integer customerId){
        return ResponseEntity.ok(service.deleteCustomer(customerId));
    }

}
