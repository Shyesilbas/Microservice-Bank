package com.serhat.creditcard.controller;

import com.serhat.creditcard.dto.CreditCardRequest;
import com.serhat.creditcard.dto.CreditCardResponse;
import com.serhat.creditcard.service.CreditCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/creditCard")
public class CreditCardController {

    private final CreditCardService creditCardService;

    @Operation(summary = "Create A Credit Card")
    @ApiResponse(responseCode = "200", description = "Credit Card Created Successfully")
    @PostMapping("/create")
    public ResponseEntity<CreditCardResponse> creatCard(@RequestBody CreditCardRequest request){
        return ResponseEntity.ok(creditCardService.createCreditCard(request));
    }

    // Do not send Request to this endpoint.
    // This endpoint connects the Credit Card and Expenses microservice to each other check and fetch the credit card Info
    // To do processes
    // Automatically
    @Operation(summary = "Find the Credit Card by it's number")
    @ApiResponse(responseCode = "200", description = "Credit Card Found Successfully")
    @GetMapping("/{cardNumber}")
    public ResponseEntity<CreditCardResponse> findCardByCardNumber(
            @PathVariable String cardNumber) {
        return ResponseEntity.ok(creditCardService.findCardByCardNumber(cardNumber));
    }

    // Do not send Request to this endpoint.
    // This endpoint updates the Card Balance end debt with CreditCardClient after a Process Done at expenses microservice
    // Automatically
    @PutMapping("/updateDebtAndBalance/{cardNumber}")
    public ResponseEntity<Void> updateDebtAndBalanceAfterProcess(
            @PathVariable String cardNumber,
            @RequestParam BigDecimal updatedDebt,
            @RequestParam BigDecimal updatedBalance) {

        creditCardService.updateDebtAndBalanceAfterProcess(cardNumber, updatedDebt, updatedBalance);
        return ResponseEntity.ok().build();
    }



}
