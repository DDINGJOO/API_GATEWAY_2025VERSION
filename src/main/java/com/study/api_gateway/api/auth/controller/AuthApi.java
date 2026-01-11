package com.study.api_gateway.api.auth.controller;

import com.study.api_gateway.api.auth.dto.request.*;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 인증 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthApi {

	@Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 새 비밀번호로 변경합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "변경 성공"),
			@ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치")
	})
	@PutMapping("/password")
	Mono<ResponseEntity<BaseResponse>> changePassword(
			@RequestBody PasswordChangeRequest req,
			ServerHttpRequest request);

	@Operation(summary = "로그인", description = "이메일/비밀번호로 로그인합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(value = """
									{
									  "isSuccess": true,
									  "code": 200,
									  "data": {
									    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
									    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
									  }
									}
									"""))),
			@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@PostMapping("/login")
	Mono<ResponseEntity<BaseResponse>> login(
			@RequestBody LoginRequest req,
			ServerHttpRequest request);

	@Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "갱신 성공"),
			@ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
	})
	@PostMapping("/refreshToken")
	Mono<ResponseEntity<BaseResponse>> refreshToken(
			@RequestBody TokenRefreshRequest req,
			ServerHttpRequest request);

	@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
	})
	@PostMapping("/signup")
	Mono<ResponseEntity<BaseResponse>> signup(
			@RequestBody SignupRequest req,
			ServerHttpRequest request);

	@Operation(summary = "이메일 인증 확인", description = "이메일 인증 코드를 확인합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증 성공"),
			@ApiResponse(responseCode = "400", description = "유효하지 않은 코드")
	})
	@GetMapping("/emails/{email}")
	Mono<ResponseEntity<BaseResponse>> confirmEmail(
			@Parameter(description = "확인할 이메일") @PathVariable(name = "email") String email,
			@Parameter(description = "인증 코드") @RequestParam String code,
			ServerHttpRequest request);

	@Operation(summary = "이메일 인증 코드 발송", description = "이메일 인증 코드를 발송합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "발송 성공")
	})
	@PostMapping("/emails/{email}")
	Mono<ResponseEntity<BaseResponse>> sendCode(
			@Parameter(description = "이메일 주소") @PathVariable(name = "email") String email,
			ServerHttpRequest request);

	@Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "탈퇴 성공")
	})
	@PostMapping("/withdraw/{userId}")
	Mono<ResponseEntity<BaseResponse>> withdraw(
			@Parameter(description = "사용자 ID") @PathVariable String userId,
			@Parameter(description = "탈퇴 사유") @RequestParam String withdrawReason,
			ServerHttpRequest request);

	@Operation(summary = "SMS 인증 코드 요청", description = "전화번호 인증을 위한 SMS 코드를 요청합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "발송 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/sms/request")
	Mono<ResponseEntity<BaseResponse>> requestSmsCode(
			@RequestBody SmsCodeRequest req,
			ServerHttpRequest request);

	@Operation(summary = "SMS 인증 코드 확인", description = "SMS로 받은 인증 코드를 확인합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "인증 성공"),
			@ApiResponse(responseCode = "400", description = "유효하지 않은 코드"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/sms/verify")
	Mono<ResponseEntity<BaseResponse>> verifySmsCode(
			@RequestBody SmsVerifyRequest req,
			ServerHttpRequest request);

	@Operation(summary = "SMS 인증 코드 재발송", description = "SMS 인증 코드를 재발송합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "재발송 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/sms/resend")
	Mono<ResponseEntity<BaseResponse>> resendSmsCode(
			@RequestBody SmsCodeRequest req,
			ServerHttpRequest request);

	@Operation(summary = "전화번호 등록 여부 확인", description = "사용자의 전화번호 등록 여부를 확인합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "확인 완료"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@GetMapping("/phone-number")
	Mono<ResponseEntity<BaseResponse>> hasPhoneNumber(
			ServerHttpRequest request);

	@Operation(summary = "카카오 소셜 로그인", description = "카카오 계정으로 로그인합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@PostMapping("/social/kakao")
	Mono<ResponseEntity<BaseResponse>> socialLoginKakao(
			@RequestBody SocialLoginRequest req,
			ServerHttpRequest request);

	@Operation(summary = "동의 항목 조회", description = "사용자의 동의 항목을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	@GetMapping("/consents")
	Mono<ResponseEntity<BaseResponse>> getConsents(
			@Parameter(description = "전체 항목 조회 여부") @RequestParam Boolean all,
			ServerHttpRequest request);

	@Operation(summary = "동의 항목 업데이트", description = "사용자의 동의 항목을 업데이트합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PatchMapping("/consent")
	Mono<ResponseEntity<BaseResponse>> updateConsent(
			@RequestBody List<ConsentRequest> consentRequests,
			ServerHttpRequest request);
}
