package com.serhat.apigateway.controller;

import com.serhat.apigateway.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class RateLimitController {
    private final RateLimitService rateLimitService;
// !!!! test
    @GetMapping("/check-rate-limit")
    public ResponseEntity<String> checkRateLimit() {
        boolean allowed = rateLimitService.tryConsume();
        if (allowed) {
            return ResponseEntity.ok("Request allowed.");
        } else {
            return ResponseEntity.status(429).body("Rate limit exceeded.");
        }
    }
}
