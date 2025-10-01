package com.study.api_gateway.controller.auth;

import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.auth.request.LoginRequest;
import com.study.api_gateway.dto.auth.request.PasswordChangeRequest;
import com.study.api_gateway.dto.auth.request.SignupRequest;
import com.study.api_gateway.dto.auth.request.TokenRefreshRequest;
import com.study.api_gateway.util.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/bff/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthClient authClient;
    private final ResponseFactory responseFactory;

    @PutMapping("/password")
    public Mono<ResponseEntity<BaseResponse>> changePassword(@RequestBody @Valid PasswordChangeRequest req, ServerHttpRequest request) {
        return authClient.changePassword(req)
                .map(result -> responseFactory.ok(result, request));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<BaseResponse>> login(@RequestBody @Valid LoginRequest req, ServerHttpRequest request) {
        return authClient.login(req.getEmail(), req.getPassword())
                .map(loginResp -> responseFactory.ok(loginResp, request));
    }

    @PostMapping("/refreshToken")
    public Mono<ResponseEntity<BaseResponse>> refreshToken(@RequestBody @Valid TokenRefreshRequest req, ServerHttpRequest request) {
        return authClient.refreshToken(req)
                .map(resp -> responseFactory.ok(resp, request));
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<BaseResponse>> signup(@RequestBody @Valid SignupRequest req, ServerHttpRequest request) {
        return authClient.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm(), req.getConsentReqs())
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        return authClient.login(req.getEmail(), req.getPassword())
                                .map(loginResp -> responseFactory.ok(loginResp, request, HttpStatus.OK));
                    }
                    return Mono.just(responseFactory.error("signup failed", HttpStatus.BAD_REQUEST, request));
                });
    }

    @GetMapping("/emails/{email}")
    public Mono<ResponseEntity<BaseResponse>> confirmEmail(@PathVariable(name = "email") String email, @RequestParam String code, ServerHttpRequest request) {
        return authClient.confirmEmail(email, code)
                .map(result -> responseFactory.ok(result, request));
    }

    @PostMapping("/emails/{email}")
    public Mono<ResponseEntity<BaseResponse>> sendCode(@PathVariable(name = "email") String email, ServerHttpRequest request) {
        return authClient.sendCode(email)
                .then(Mono.just(responseFactory.ok("code sent", request)));
    }

    @PostMapping("/withdraw/{userId}")
    public Mono<ResponseEntity<BaseResponse>> withdraw(@PathVariable String userId, @RequestParam String withdrawReason, ServerHttpRequest request) {
        return authClient.withdraw(userId, withdrawReason)
                .map(result -> responseFactory.ok(result, request));
    }
}
