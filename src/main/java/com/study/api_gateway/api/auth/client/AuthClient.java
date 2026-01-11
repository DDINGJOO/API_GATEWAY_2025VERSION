package com.study.api_gateway.api.auth.client;


import com.study.api_gateway.api.auth.dto.request.*;
import com.study.api_gateway.api.auth.dto.response.ConsentsTable;
import com.study.api_gateway.api.auth.dto.response.LoginResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthClient {
	private final WebClient webClient;
	
	public AuthClient(@Qualifier(value = "authWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	
	public Mono<LoginResponse> login(String email, String password) {
		
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/login")
				.toUriString();
		
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(new LoginRequest(email, password))
				.retrieve()
				.bodyToMono(LoginResponse.class);
	}
	
	public Mono<Boolean> signup(String email, String password, String passwordConfirm, List<ConsentRequest> consentReq) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/signup")
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(new SignupRequest(email, password, passwordConfirm, consentReq))
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<Boolean> confirmEmail(String email, String code) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/emails/{email}")
				.queryParam("code", code)
				.buildAndExpand(email)
				.toUriString();
		
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<Void> sendCode(String email) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/emails/{email}")
				.buildAndExpand(email)
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	public Mono<LoginResponse> refreshToken(TokenRefreshRequest req) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/login/refreshToken")
				.toUriString();
		
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(req)
				.retrieve()
				.bodyToMono(LoginResponse.class);
	}
	
	public Mono<Boolean> changePassword(PasswordChangeRequest req) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/password")
				.queryParam("email", req.getEmail())
				.queryParam("newPassword", req.getNewPassword())
				.queryParam("passConfirm", req.getNewPasswordConfirm())
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<Boolean> withdraw(String userId, String withdrawReason) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/withdraw/{userId}")
				.queryParam("withdrawReason", withdrawReason)
				.buildAndExpand(userId)
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<Map<String, ConsentsTable>> fetchAllConsents(Boolean all) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/consents")
				.queryParam("all", all)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, ConsentsTable>>() {
				});
	}
	
	public Mono<Boolean> hasPhoneNumber(String userId) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/phone-number/{userId}")
				.buildAndExpand(userId)
				.toUriString();

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Boolean.class)
				.onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.NotFound.class,
						e -> Mono.just(false));
	}

	public Mono<Void> requestSmsCode(String userId, String phoneNumber) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/sms/request")
				.toUriString();

		return webClient.post()
				.uri(uriString)
				.bodyValue(Map.of("userId", userId, "phoneNumber", phoneNumber))
				.retrieve()
				.bodyToMono(Void.class);
	}

	public Mono<Boolean> verifySmsCode(String userId, String phoneNumber, String code) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/sms/verify")
				.toUriString();

		return webClient.post()
				.uri(uriString)
				.bodyValue(Map.of("userId", userId, "phoneNumber", phoneNumber, "code", code))
				.retrieve()
				.bodyToMono(Boolean.class);
	}

	public Mono<Boolean> resendSmsCode(String userId, String phoneNumber) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/sms/resend")
				.toUriString();

		return webClient.post()
				.uri(uriString)
				.bodyValue(Map.of("userId", userId, "phoneNumber", phoneNumber))
				.retrieve()
				.bodyToMono(Boolean.class);
	}
	
	public Mono<LoginResponse> socialLoginKakao(String accessToken) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/social/kakao")
				.toUriString();

		return webClient.post()
				.uri(uriString)
				.bodyValue(Map.of("accessToken", accessToken))
				.retrieve()
				.bodyToMono(LoginResponse.class);
	}
	
	public Mono<Boolean> updateConsents(String userId, List<ConsentRequest> consentRequests) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/auth/consent/{userId}")
				.buildAndExpand(userId)
				.toUriString();
		
		return webClient.patch()
				.uri(uriString)
				.bodyValue(consentRequests)
				.retrieve()
				.bodyToMono(Boolean.class);
	}
}
