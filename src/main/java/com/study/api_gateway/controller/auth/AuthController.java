package com.study.api_gateway.controller.auth;

import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.auth.request.*;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class AuthController {
	private final AuthClient authClient;
	private final ResponseFactory responseFactory;
	
	@Operation(summary = "비밀번호 변경")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "PasswordChangeSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/password\"\n  }\n}")))
	})
	@PutMapping("/password")
	public Mono<ResponseEntity<BaseResponse>> changePassword(@RequestBody @Valid PasswordChangeRequest req, ServerHttpRequest request) {
		return authClient.changePassword(req)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "로그인")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "LoginSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"accessToken\": \"<JWT>\",\n    \"refreshToken\": \"<JWT_REFRESH>\",\n    \"deviceId\": \"device-123\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/login\"\n  }\n}")))
	})
	@PostMapping("/login")
	public Mono<ResponseEntity<BaseResponse>> login(@RequestBody @Valid LoginRequest req, ServerHttpRequest request) {
		return authClient.login(req.getEmail(), req.getPassword())
				.map(loginResp -> responseFactory.ok(loginResp, request));
	}
	
	@Operation(summary = "토큰 리프레시")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "RefreshTokenSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"accessToken\": \"<JWT_NEW>\",\n    \"refreshToken\": \"<JWT_REFRESH>\",\n    \"deviceId\": \"device-123\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/refreshToken\"\n  }\n}")))
	})
	@PostMapping("/refreshToken")
	public Mono<ResponseEntity<BaseResponse>> refreshToken(@RequestBody @Valid TokenRefreshRequest req, ServerHttpRequest request) {
		return authClient.refreshToken(req)
				.map(resp -> responseFactory.ok(resp, request));
	}
	
	@Operation(summary = "회원가입")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SignupSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"accessToken\": \"<JWT>\",\n    \"refreshToken\": \"<JWT_REFRESH>\",\n    \"deviceId\": \"device-123\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/signup\"\n  }\n}")))
	})
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
	
	@Operation(summary = "이메일 인증 확인")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ConfirmEmailSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/emails/{email}\"\n  }\n}")))
	})
	@GetMapping("/emails/{email}")
	public Mono<ResponseEntity<BaseResponse>> confirmEmail(@PathVariable(name = "email") String email, @RequestParam String code, ServerHttpRequest request) {
		return authClient.confirmEmail(email, code)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "이메일 인증코드 발송")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SendCodeSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"code sent\",\n  \"request\": {\n    \"path\": \"/bff/v1/auth/emails/{email}\"\n  }\n}")))
	})
	@PostMapping("/emails/{email}")
	public Mono<ResponseEntity<BaseResponse>> sendCode(@PathVariable(name = "email") String email, ServerHttpRequest request) {
		return authClient.sendCode(email)
				.then(Mono.just(responseFactory.ok("code sent", request)));
	}
	
	@Operation(summary = "회원 탈퇴")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "WithdrawSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/withdraw/{userId}\"\n  }\n}")))
	})
	@PostMapping("/withdraw/{userId}")
	public Mono<ResponseEntity<BaseResponse>> withdraw(@PathVariable String userId, @RequestParam String withdrawReason, ServerHttpRequest request) {
		return authClient.withdraw(userId, withdrawReason)
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "SMS 인증 코드 요청", description = "휴대폰 번호로 인증 코드를 발송합니다. (Kafka 이벤트 발행)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SmsRequestSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"인증 코드가 발송되었습니다\",\n  \"request\": {\n    \"path\": \"/bff/v1/auth/sms/request\"\n  }\n}"))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"사용자 인증 정보를 찾을 수 없습니다\"\n}")))
	})
	@PostMapping("/sms/request")
	public Mono<ResponseEntity<BaseResponse>> requestSmsCode(@RequestBody @Valid SmsCodeRequest req, ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authClient.requestSmsCode(userId, req.getPhoneNumber())
				.then(Mono.just(responseFactory.ok("인증 코드가 발송되었습니다", request)));
	}

	@Operation(summary = "SMS 인증 확인", description = "발송된 인증 코드를 검증합니다. 성공 시 전화번호가 암호화되어 저장됩니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SmsVerifySuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/sms/verify\"\n  }\n}"))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"사용자 인증 정보를 찾을 수 없습니다\"\n}")))
	})
	@PostMapping("/sms/verify")
	public Mono<ResponseEntity<BaseResponse>> verifySmsCode(@RequestBody @Valid SmsVerifyRequest req, ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authClient.verifySmsCode(userId, req.getPhoneNumber(), req.getCode())
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "SMS 인증 코드 재발송", description = "인증 코드를 재발송합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SmsResendSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/sms/resend\"\n  }\n}"))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"사용자 인증 정보를 찾을 수 없습니다\"\n}")))
	})
	@PostMapping("/sms/resend")
	public Mono<ResponseEntity<BaseResponse>> resendSmsCode(@RequestBody @Valid SmsCodeRequest req, ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authClient.resendSmsCode(userId, req.getPhoneNumber())
				.map(result -> responseFactory.ok(result, request));
	}

	@Operation(summary = "휴대폰 등록 여부 확인", description = "사용자의 휴대폰 번호 등록 여부를 확인합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "HasPhoneNumberSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/phone-number\"\n  }\n}"))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"사용자 인증 정보를 찾을 수 없습니다\"\n}")))
	})
	@GetMapping("/phone-number")
	public Mono<ResponseEntity<BaseResponse>> hasPhoneNumber(ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authClient.hasPhoneNumber(userId)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "카카오 소셜 로그인", description = "카카오 액세스 토큰으로 로그인합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "KakaoLoginSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"accessToken\": \"<JWT>\",\n    \"refreshToken\": \"<JWT_REFRESH>\",\n    \"deviceId\": \"device-123\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/social/kakao\"\n  }\n}"))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"카카오 인증에 실패했습니다\"\n}")))
	})
	@PostMapping("/social/kakao")
	public Mono<ResponseEntity<BaseResponse>> socialLoginKakao(@RequestBody @Valid SocialLoginRequest req, ServerHttpRequest request) {
		return authClient.socialLoginKakao(req.getAccessToken())
				.map(loginResp -> responseFactory.ok(loginResp, request));
	}
	
	@Operation(summary = "약관 목록 조회", description = "시스템에서 사용 가능한 약관 목록을 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ConsentsListSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"TERMS_OF_SERVICE_v1.0\": {\n      \"id\": \"TERMS_OF_SERVICE_v1.0\",\n      \"consentName\": \"TERMS_OF_SERVICE\",\n      \"version\": \"v1.0\",\n      \"consentUrl\": \"https://example.com/terms\",\n      \"required\": true\n    }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/auth/consents\"\n  }\n}")))
	})
	@GetMapping("/consents")
	public Mono<ResponseEntity<BaseResponse>> getConsents(@RequestParam Boolean all, ServerHttpRequest request) {
		return authClient.fetchAllConsents(all)
				.map(result -> responseFactory.ok(result, request));
	}
	
	@Operation(summary = "약관 동의 정보 변경", description = "사용자의 선택 약관 동의/철회를 처리합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ConsentUpdateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": true,\n  \"request\": {\n    \"path\": \"/bff/v1/auth/consent\"\n  }\n}"))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 400,\n  \"data\": \"REQUIRED_CONSENT_CANNOT_BE_REVOKED\"\n}"))),
			@ApiResponse(responseCode = "401", description = "인증 실패",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n  \"isSuccess\": false,\n  \"code\": 401,\n  \"data\": \"사용자 인증 정보를 찾을 수 없습니다\"\n}")))
	})
	@PatchMapping("/consent")
	public Mono<ResponseEntity<BaseResponse>> updateConsent(
			@RequestBody @Valid List<ConsentRequest> consentRequests,
			ServerHttpRequest request) {
		String userId = request.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isEmpty()) {
			return Mono.just(responseFactory.error("사용자 인증 정보를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED, request));
		}
		return authClient.updateConsents(userId, consentRequests)
				.map(result -> responseFactory.ok(result, request));
	}
}
