package com.study.api_gateway.client;


import com.study.api_gateway.dto.auth.request.*;
import com.study.api_gateway.dto.auth.response.LoginResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthClient {
    private final WebClient webClient;

    public AuthClient(@Qualifier(value = "authWebClient") WebClient webClient) {
        this.webClient = webClient;

    }


    public Mono<LoginResponse> login(String email, String password) {

        String uriString = UriComponentsBuilder.fromPath("/api/auth/login")
                .toUriString();


        return webClient.post()
                .uri(uriString)
                .bodyValue(new LoginRequest(email, password))
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }

    public Mono<Boolean> signup(String email, String password, String passwordConfirm, List<ConsentRequest> consentReq) {
        String uriString = UriComponentsBuilder.fromPath("/api/auth/signup")
                .toUriString();

        return webClient.post()
                .uri(uriString)
                .bodyValue(new SignupRequest(email, password, passwordConfirm, consentReq))
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> confirmEmail(String email,  String code) {
        String uriString = UriComponentsBuilder.fromPath("/api/auth/emails/{email}")
                .queryParam("code", code)
                .buildAndExpand(email)
                .toUriString();


        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> resendEmail(String email) {
        String uriString = UriComponentsBuilder.fromPath("/api/auth/emails/{email}")
                .buildAndExpand(email)
                .toUriString();

        return webClient.post()
                .uri(uriString)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> sendCode(String email) {
        String uriString = UriComponentsBuilder.fromPath("/api/auth/emails/{email}/code")
                .buildAndExpand(email)
                .toUriString();

        return webClient.post()
                .uri(uriString)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<LoginResponse> refreshToken(TokenRefreshRequest req) {
        String uriString = UriComponentsBuilder.fromPath("/api/auth/login/refreshToken")
                .toUriString();


        return webClient.post()
                .uri(uriString)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }

    public Mono<Boolean> changePassword(PasswordChangeRequest req) {
        String uriString = UriComponentsBuilder.fromPath("/api/auth/passwordChange/changePassword")
                .toUriString();

        return webClient.put()
                .uri(uriString)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
