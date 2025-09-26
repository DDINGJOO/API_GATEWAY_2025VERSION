package com.study.api_gateway.controller.auth;


import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.dto.auth.request.LoginRequest;
import com.study.api_gateway.dto.auth.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthClient authClient;

    @GetMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest req) {
        return authClient.login(req.getEmail(), req.getPassword());
    }
}
