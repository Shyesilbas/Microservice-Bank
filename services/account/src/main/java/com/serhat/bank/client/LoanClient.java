package com.serhat.bank.client;

import com.serhat.bank.dto.LoanResponse;
import com.serhat.bank.dto.LoanResponseForTotalPayment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service",url = "http://localhost:8020/api/v1/loan")
public interface LoanClient {


  @GetMapping("/{loanId}")
  @CircuitBreaker(name = "LoanServiceCircuitBreaker")
  LoanResponse findByLoanId(@PathVariable Integer loanId);

  @GetMapping("/detailed/{loanId}")
  @CircuitBreaker(name = "LoanServiceCircuitBreaker")
  LoanResponseForTotalPayment findLoanById(@PathVariable Integer loanId);
}
