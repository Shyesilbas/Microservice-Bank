package com.serhat.creditcard.controller;

import com.serhat.creditcard.dto.CreditCardRequest;
import com.serhat.creditcard.dto.CreditCardResponse;
import com.serhat.creditcard.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/creditCard")
public class CreditCardController {

    private final CreditCardService creditCardService;


    @PostMapping("/create")
    public ResponseEntity<CreditCardResponse> creatCard(@RequestBody CreditCardRequest request){
        return ResponseEntity.ok(creditCardService.createCreditCard(request));
    }

}
