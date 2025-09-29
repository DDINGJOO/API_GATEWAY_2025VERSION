package com.study.api_gateway.controller;

import com.study.api_gateway.dto.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public Mono<ResponseEntity<BaseResponse>> health() {
        return Mono.just(BaseResponse.success("ok", Map.of("path", "/health")));
    }
}
