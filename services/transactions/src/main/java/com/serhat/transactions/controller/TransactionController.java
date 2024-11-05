package com.serhat.transactions.controller;

import com.serhat.transactions.dto.*;
import com.serhat.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService service;



    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@RequestBody DepositRequest request){
        return ResponseEntity.ok(service.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(@RequestBody WithdrawRequest request){
        return ResponseEntity.ok(service.withdraw(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request){
        return ResponseEntity.ok(service.transfer(request));
    }

    @GetMapping("/depositHistory")
    public ResponseEntity<List<DepositHistory>> depositHistory(@RequestParam String accountNumber) {
        return ResponseEntity.ok(service.depositHistories(accountNumber));
    }

    @GetMapping("/withdrawHistory")
    public ResponseEntity<List<WithdrawHistory>> withdrawHistory(@RequestParam String accountNumber) {
        return ResponseEntity.ok(service.withdrawHistories(accountNumber));
    }



}
