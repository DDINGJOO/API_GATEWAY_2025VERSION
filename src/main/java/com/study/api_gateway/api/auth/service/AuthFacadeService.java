package com.study.api_gateway.api.auth.service;

import com.study.api_gateway.api.auth.client.AuthClient;
import com.study.api_gateway.api.auth.dto.request.ConsentRequest;
import com.study.api_gateway.api.auth.dto.request.PasswordChangeRequest;
import com.study.api_gateway.api.auth.dto.request.TokenRefreshRequest;
import com.study.api_gateway.api.auth.dto.response.ConsentsTable;
import com.study.api_gateway.api.auth.dto.response.LoginResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Auth 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class AuthFacadeService {
	
	private static final String SERVICE_NAME = "auth-service";
	private final AuthClient authClient;
	private final ResilienceOperator resilience;
	
	public Mono<LoginResponse> login(String email, String password) {
		return authClient.login(email, password)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> signup(String email, String password, String passwordConfirm, List<ConsentRequest> consentReqs) {
		return authClient.signup(email, password, passwordConfirm, consentReqs)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<LoginResponse> refreshToken(TokenRefreshRequest req) {
		return authClient.refreshToken(req)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> changePassword(PasswordChangeRequest req) {
		return authClient.changePassword(req)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> confirmEmail(String email, String code) {
		return authClient.confirmEmail(email, code)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> sendCode(String email) {
		return authClient.sendCode(email)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> withdraw(String userId, String withdrawReason) {
		return authClient.withdraw(userId, withdrawReason)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> requestSmsCode(String userId, String phoneNumber) {
		return authClient.requestSmsCode(userId, phoneNumber)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> verifySmsCode(String userId, String phoneNumber, String code) {
		return authClient.verifySmsCode(userId, phoneNumber, code)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> resendSmsCode(String userId, String phoneNumber) {
		return authClient.resendSmsCode(userId, phoneNumber)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> hasPhoneNumber(String userId) {
		return authClient.hasPhoneNumber(userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<LoginResponse> socialLoginKakao(String accessToken) {
		return authClient.socialLoginKakao(accessToken)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, ConsentsTable>> fetchAllConsents(Boolean all) {
		return authClient.fetchAllConsents(all)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Boolean> updateConsents(String userId, List<ConsentRequest> consentRequests) {
		return authClient.updateConsents(userId, consentRequests)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
