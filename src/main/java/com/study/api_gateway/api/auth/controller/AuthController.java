package com.study.api_gateway.api.auth.controller;

import com.study.api_gateway.api.auth.service.AuthFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.api.auth.dto.request.*;
import com.study.api_gateway.common.response.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController implements AuthApi {
	private final AuthFacadeService authFacadeService;
	private final ResponseFactory responseFactory;
	
	@Override
	@PutMapping("/password")
	public Mono<ResponseEntity<BaseResponse>> changePassword(@RequestBody @Valid PasswordChangeRequest req, ServerHttpRequest request) {
		return authFacadeService.changePassword(req)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@PostMapping("/login")
	public Mono<ResponseEntity<BaseResponse>> login(@RequestBody @Valid LoginRequest req, ServerHttpRequest request) {
		return authFacadeService.login(req.getEmail(), req.getPassword())
				.map(loginResp -> responseFactory.ok(loginResp, request));
	}
	
	@Override
	@PostMapping("/refreshToken")
	public Mono<ResponseEntity<BaseResponse>> refreshToken(@RequestBody @Valid TokenRefreshRequest req, ServerHttpRequest request) {
		return authFacadeService.refreshToken(req)
				.map(resp -> responseFactory.ok(resp, request));
	}
	
	@Override
	@PostMapping("/signup")
	public Mono<ResponseEntity<BaseResponse>> signup(@RequestBody @Valid SignupRequest req, ServerHttpRequest request) {
		return authFacadeService.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm(), req.getConsentReqs())
				.flatMap(success -> {
					if (Boolean.TRUE.equals(success)) {
						return authFacadeService.login(req.getEmail(), req.getPassword())
								.map(loginResp -> responseFactory.ok(loginResp, request, HttpStatus.OK));
					}
					return Mono.just(responseFactory.error("signup failed", HttpStatus.BAD_REQUEST, request));
				});
	}
	
	@Override
	@GetMapping("/emails/{email}")
	public Mono<ResponseEntity<BaseResponse>> confirmEmail(@PathVariable(name = "email") String email, @RequestParam String code, ServerHttpRequest request) {
		return authFacadeService.confirmEmail(email, code)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@PostMapping("/emails/{email}")
	public Mono<ResponseEntity<BaseResponse>> sendCode(@PathVariable(name = "email") String email, ServerHttpRequest request) {
		return authFacadeService.sendCode(email)
				.then(Mono.just(responseFactory.ok("code sent", request)));
	}
	
	@Override
	@PostMapping("/withdraw/{userId}")
	public Mono<ResponseEntity<BaseResponse>> withdraw(@PathVariable String userId, @RequestParam String withdrawReason, ServerHttpRequest request) {
		return authFacadeService.withdraw(userId, withdrawReason)
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@PostMapping("/sms/request")
	public Mono<ResponseEntity<BaseResponse>> requestSmsCode(@RequestBody @Valid SmsCodeRequest req, ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authFacadeService.requestSmsCode(userId, req.getPhoneNumber())
				.then(Mono.just(responseFactory.ok("인증 코드가 발송되었습니다", request)));
	}

	@Override
	@PostMapping("/sms/verify")
	public Mono<ResponseEntity<BaseResponse>> verifySmsCode(@RequestBody @Valid SmsVerifyRequest req, ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authFacadeService.verifySmsCode(userId, req.getPhoneNumber(), req.getCode())
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@PostMapping("/sms/resend")
	public Mono<ResponseEntity<BaseResponse>> resendSmsCode(@RequestBody @Valid SmsCodeRequest req, ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authFacadeService.resendSmsCode(userId, req.getPhoneNumber())
				.map(result -> responseFactory.ok(result, request));
	}

	@Override
	@GetMapping("/phone-number")
	public Mono<ResponseEntity<BaseResponse>> hasPhoneNumber(ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authFacadeService.hasPhoneNumber(userId)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@PostMapping("/social/kakao")
	public Mono<ResponseEntity<BaseResponse>> socialLoginKakao(@RequestBody @Valid SocialLoginRequest req, ServerHttpRequest request) {
		return authFacadeService.socialLoginKakao(req.getAccessToken())
				.map(loginResp -> responseFactory.ok(loginResp, request));
	}
	
	@Override
	@GetMapping("/consents")
	public Mono<ResponseEntity<BaseResponse>> getConsents(@RequestParam Boolean all, ServerHttpRequest request) {
		return authFacadeService.fetchAllConsents(all)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Override
	@PatchMapping("/consent")
	public Mono<ResponseEntity<BaseResponse>> updateConsent(
			@RequestBody @Valid List<ConsentRequest> consentRequests,
			ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authFacadeService.updateConsents(userId, consentRequests)
				.map(result -> responseFactory.ok(result, request));
	}
}
