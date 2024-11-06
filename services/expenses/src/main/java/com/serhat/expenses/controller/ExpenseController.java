package com.serhat.expenses.controller;

import com.serhat.expenses.dto.ProcessRequest;
import com.serhat.expenses.dto.ProcessResponse;
import com.serhat.expenses.service.ExpensesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpensesService service;


    @Operation(summary = "Pay By Credit Card")
    @ApiResponse(responseCode = "200", description = "Payment Done Successfully")
    @PostMapping("/doProcess")
    public ResponseEntity<ProcessResponse> doProcess(@RequestBody ProcessRequest request){
        return ResponseEntity.ok(service.doPayment(request));
    }
}
