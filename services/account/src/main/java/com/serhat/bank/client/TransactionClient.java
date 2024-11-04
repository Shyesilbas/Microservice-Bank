package com.serhat.bank.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service",url = "http://localhost:8050/api/v1/transactions")
public interface TransactionClient {



  //  @PostMapping("/transfer")
//    TransferResponse createTransferTransaction(@RequestBody TransferRequest request);



}
