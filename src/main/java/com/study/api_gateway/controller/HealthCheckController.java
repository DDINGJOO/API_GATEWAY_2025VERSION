package com.study.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Mono<String>> health() {
        return ResponseEntity.ok(Mono.just("ok"));
    }
}
