package com.serhat.bank.controller;

import com.serhat.bank.client.DepositRequest;
import com.serhat.bank.client.DepositResponse;
import com.serhat.bank.client.WithdrawRequest;
import com.serhat.bank.client.WithdrawResponse;
import com.serhat.bank.dto.*;
import com.serhat.bank.model.Account;
import com.serhat.bank.model.Currency;
import com.serhat.bank.service.AccountService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

//    @Operation(summary = "Create a new Account")
//    @ApiResponse(responseCode = "200", description = "Account Created Successfully")
    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest request) {
       return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping("/byAccountNumber/{accountNumber}")
    public ResponseEntity<AccountResponse> findByAccountNumber(@PathVariable int accountNumber){
        return ResponseEntity.ok(accountService.findByAccountNumber(accountNumber));
    }
    @PutMapping("/debtPayment/{accountNumber}")
    public ResponseForDebtPayment updateBalanceAfterCardDebtPayment(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal updatedBalance) {

        return accountService.updateBalanceAfterCardDebtPayment(accountNumber, updatedBalance);
    }

//    @Operation(summary = "Deposit to an Account")
//    @ApiResponse(responseCode = "200", description = "Deposit Successful")
    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@RequestBody DepositRequest request) throws AccountNotFoundException {
        DepositResponse depositResponse = accountService.updateBalanceAfterDeposit(request);
        return ResponseEntity.ok(depositResponse);
    }
//    @Operation(summary = "Loan Payment")
//    @ApiResponse(responseCode = "200", description = "Successful")
    @PostMapping("/loanPayment")
    public ResponseEntity<LoanInstallmentPaymentResponse> payLoanInstallment(@RequestBody LoanPaymentRequest request) throws AccountNotFoundException {
        LoanInstallmentPaymentResponse response = accountService.updateBalanceAfterLoanPayment(request);
        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "Loan Payment")
//    @ApiResponse(responseCode = "200", description = "Successful")
    @PostMapping("/loanTotalPayment")
    public ResponseEntity<payTotalLoanDebtResponse> payTotalLoan(@RequestBody payTotalLoanDebtRequest request) throws AccountNotFoundException {
        payTotalLoanDebtResponse response = accountService.updateBalanceAfterTotalLoanPayment(request);
        return ResponseEntity.ok(response);
    }



//    @Operation(summary = "Loan to an Account")
//    @ApiResponse(responseCode = "200", description = "Loan Transaction Successful")
    @PostMapping("/loan")
    public ResponseEntity<LoanResponse> deposit(@RequestBody LoanRequest request) throws AccountNotFoundException {
        LoanResponse loanResponse = accountService.updateBalanceAfterLoanApplication(request);
        return ResponseEntity.ok(loanResponse);
    }

//    @Operation(summary = "Withdraw from an Account")
//    @ApiResponse(responseCode = "200", description = "Withdrawal Successful")
    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(@RequestBody WithdrawRequest request) throws AccountNotFoundException {
        WithdrawResponse withdrawResponse = accountService.updateBalanceAfterWithdraw(request);
        return ResponseEntity.ok(withdrawResponse);
    }


//    @Operation(summary = "Update related credit card count for a customer")
//    @ApiResponse(responseCode = "200", description = "Credit card count updated for the customer")
    @PostMapping("/creditCards/{accountId}")
    public ResponseEntity<Void> updateLinkedCreditCards(@PathVariable Integer accountId) {
        accountService.updateLinkedCreditCards(accountId);
        return ResponseEntity.ok().build();
    }

//    @Operation(summary = "Fetch All Accounts")
//    @ApiResponse(responseCode = "200", description = "Accounts Fetched Successfully")
    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAllAccounts() {
        List<AccountResponse> accounts = accountService.findAllAccounts();
        return ResponseEntity.ok(accounts);
    }

//    @Operation(summary = "Get Account by ID")
//    @ApiResponse(responseCode = "200", description = "Account Fetched Successfully")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Integer id) {
        AccountResponse account = accountService.findById(id);
        return ResponseEntity.ok(account);
    }
//    @Operation(summary = "Delete an Account")
//    @ApiResponse(responseCode = "200", description = "Account Deleted Successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Integer id) {
        String response = accountService.deleteAccount(id);
        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "Account(s) For specific Customer")
//    @ApiResponse(responseCode = "200",description = "Account For the Customer Id's")
    @GetMapping("/byCustomer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable Integer customerId) {
        return ResponseEntity.ok(accountService.findAccountByCustomerId(customerId));
    }

     @GetMapping("/byCurrency/{customerId}/{currency}")
    public ResponseEntity<List<AccountResponse>> findAccountsByCurrencyForSpecialCustomer(@PathVariable Integer customerId , @PathVariable Currency currency){
        return ResponseEntity.ok(accountService.findAccountsByCurrencyForSpecialCustomer(customerId,currency));
     }



}
