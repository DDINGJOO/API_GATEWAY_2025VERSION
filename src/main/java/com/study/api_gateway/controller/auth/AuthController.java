package com.study.api_gateway.controller.auth;

import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.auth.request.LoginRequest;
import com.study.api_gateway.dto.auth.request.PasswordChangeRequest;
import com.study.api_gateway.dto.auth.request.SignupRequest;
import com.study.api_gateway.dto.auth.request.TokenRefreshRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/password")
    public Mono<ResponseEntity<BaseResponse>> changePassword(@RequestBody @Valid PasswordChangeRequest req) {
        return authClient.changePassword(req)
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/auth/password")));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<BaseResponse>> login(@RequestBody @Valid LoginRequest req) {
        return authClient.login(req.getEmail(), req.getPassword())
                .map(loginResp -> BaseResponse.success(loginResp, Map.of("path", "/bff/v1/auth/login")));
    }

    @PostMapping("/refreshToken")
    public Mono<ResponseEntity<BaseResponse>> refreshToken(@RequestBody @Valid TokenRefreshRequest req) {
        return authClient.refreshToken(req)
                .map(resp -> BaseResponse.success(resp, Map.of("path", "/bff/v1/auth/refreshToken")));
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<BaseResponse>> signup(@RequestBody @Valid SignupRequest req) {
        return authClient.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm(), req.getConsentReqs())
                .flatMap(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        return authClient.login(req.getEmail(), req.getPassword())
                                .map(loginResp -> BaseResponse.success(loginResp, Map.of("path", "/bff/v1/auth/signup"), HttpStatus.OK));
                    }
                    return Mono.just(BaseResponse.error("signup failed", HttpStatus.BAD_REQUEST, Map.of("path", "/bff/v1/auth/signup")));
                });
    }

    @GetMapping("/emails/{email}")
    public Mono<ResponseEntity<BaseResponse>> confirmEmail(@PathVariable(name = "email") String email, @RequestParam String code) {
        return authClient.confirmEmail(email, code)
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/auth/emails/" + email)));
    }

    @PostMapping("/emails/{email}")
    public Mono<ResponseEntity<BaseResponse>> sendCode(@PathVariable(name = "email") String email) {
        return authClient.sendCode(email)
                .then(Mono.just(BaseResponse.success("code sent", Map.of("path", "/bff/v1/auth/emails/" + email))));
    }

    @PostMapping("/withdraw/{userId}")
    public Mono<ResponseEntity<BaseResponse>> withdraw(@PathVariable String userId, @RequestParam String withdrawReason) {
        return authClient.withdraw(userId, withdrawReason)
                .map(result -> BaseResponse.success(result, Map.of("path", "/bff/v1/auth/withdraw/" + userId)));
    }
}
