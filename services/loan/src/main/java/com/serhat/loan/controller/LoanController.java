package com.serhat.loan.controller;

import com.serhat.loan.dto.LoanInstallmentPayRequest;
import com.serhat.loan.dto.LoanInstallmentPaymentResponse;
import com.serhat.loan.dto.LoanRequest;
import com.serhat.loan.dto.LoanResponse;
import com.serhat.loan.entity.Loan;
import com.serhat.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loan")
public class LoanController {
    private final LoanService service;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> applyToLoan(@RequestBody LoanRequest request){
        return ResponseEntity.ok(service.applyToLoan(request));
    }
    @GetMapping("/{loanId}")
    public LoanResponse loanById(@PathVariable Integer loanId){
        return service.findByLoanId(loanId);
    }

    @PostMapping("/payLoanInstallment")
    public ResponseEntity<LoanInstallmentPaymentResponse> payLoanInstallment(@RequestBody LoanInstallmentPayRequest request){
        return ResponseEntity.ok(service.payLoanInstallment(request));
    }
}
