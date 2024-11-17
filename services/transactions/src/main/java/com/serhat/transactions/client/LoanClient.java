package com.serhat.transactions.client;


import com.serhat.transactions.dto.LoanResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service",url = "http://localhost:8020/api/v1/loan")
public interface LoanClient {


  @GetMapping("/{loanId}")
  @CircuitBreaker(name = "loanCircuitBreaker")

  LoanResponse findByLoanId(@PathVariable Integer loanId);


}
