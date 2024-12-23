package com.serhat.expenses.controller;

import com.serhat.expenses.dto.PaymentResponse;
import com.serhat.expenses.dto.ProcessRequest;
import com.serhat.expenses.dto.ProcessResponse;
import com.serhat.expenses.entity.Category;
import com.serhat.expenses.service.ExpensesService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpensesService service;

//    @Operation(summary = "Pay By Credit Card")
//    @ApiResponse(responseCode = "200", description = "Payment Done Successfully")
    @PostMapping("/doProcess")
    public ResponseEntity<ProcessResponse> doProcess(@RequestBody ProcessRequest request){
        return ResponseEntity.ok(service.doPayment(request));
    }

//    @Operation(summary = "Credit Card payment History For the Customer Id")
//    @ApiResponse(responseCode = "200", description = "History Fetched")
    @GetMapping("/history/byCustomerId/{customerId}")
    public List<PaymentResponse> paymentHistoryByCustomerId(
           @PathVariable Integer customerId){
        return service.paymentHistoryByCustomerId(customerId);
    }

//    @Operation(summary = "Credit Card payment History For the Card Number")
//    @ApiResponse(responseCode = "200", description = "History Fetched")
    @GetMapping("/history/byCardNumber/{cardNumber}")
    public List<PaymentResponse> paymentHistoryByCardNumber(@PathVariable  String cardNumber){
        return service.paymentHistoryByCardNumber(cardNumber);
    }

    @GetMapping("/history/byCategory/{cardNumber}/{category}")
    public ResponseEntity<List<PaymentResponse>> paymentHistoryByCategory(@PathVariable String cardNumber , @PathVariable Category category){
        return ResponseEntity.ok(service.processHistoryByCategory(cardNumber, category));
    }


}
