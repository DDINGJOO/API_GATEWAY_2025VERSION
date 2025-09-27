package com.study.api_gateway.controller.auth;


import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.dto.auth.request.LoginRequest;
import com.study.api_gateway.dto.auth.request.PasswordChangeRequest;
import com.study.api_gateway.dto.auth.request.SignupRequest;
import com.study.api_gateway.dto.auth.request.TokenRefreshRequest;
import com.study.api_gateway.dto.auth.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthClient authClient;
    @PutMapping("/password")
    public Mono<Boolean> changePassword(@RequestBody PasswordChangeRequest req) {
        return authClient.changePassword(req);
    }

    @GetMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest req) {
        return authClient.login(req.getEmail(), req.getPassword());
    }

    @PostMapping("/refreshToken")
    public Mono<LoginResponse> refreshToken(@RequestBody TokenRefreshRequest req) {
        return authClient.refreshToken(req);
    }

    //TODO : 에러 컨벤션 및 베이스 리스폰스 객체 생성 고려
    @PostMapping("/signup")
    public Mono<Boolean> signup(@RequestBody SignupRequest req) {
        var result =   authClient.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm(), req.getConsentReqs());
        return result.flatMap(success -> {
            if (Boolean.TRUE.equals(success)) {
                return login(new LoginRequest(req.getEmail(), req.getPassword()));
            }
            return Mono.empty();
        })
        .onErrorMap(throwable -> {
            if (throwable instanceof WebClientResponseException webEx) {
                return new ResponseStatusException(
                        webEx.getStatusCode(),
                        webEx.getResponseBodyAsString(),
                        webEx
                );
            }
            return throwable;
        }).hasElement();
    }



    @GetMapping("/emails/{email}")
    public Mono<Boolean> confirmEmail(@PathVariable(name= "email") String email,@RequestParam String code) {
        return authClient.confirmEmail(email, code);
    }

    @PostMapping("/emails/{email}")
    public Mono<Boolean> resendEmail(@PathVariable(name= "email") String email) {
        return authClient.resendEmail(email);
    }

    @PostMapping("/emails/{email}/code")
    public Mono<Boolean> sendCode(@PathVariable(name= "email") String email) {
        return authClient.sendCode(email);
    }

    @PostMapping("/withdraw/{userId}")
    public Mono<Boolean> withdraw(@PathVariable String userId, @RequestParam String withdrawReason) {
        return authClient.withdraw(userId, withdrawReason);
    }

}
