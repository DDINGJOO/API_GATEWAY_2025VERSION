package com.study.api_gateway.api.notification.controller;

import com.study.api_gateway.api.notification.dto.request.DeleteDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.RegisterDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.UpdateNightAdConsentRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 알림 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

	// ==================== 디바이스 토큰 API ====================

	@Operation(summary = "디바이스 토큰 등록", description = "FCM 푸시 알림을 위한 디바이스 토큰을 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "토큰 등록 성공")
	})
	@PostMapping("/devices/token")
	Mono<ResponseEntity<BaseResponse>> registerDeviceToken(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			@RequestBody RegisterDeviceTokenRequest request,
			ServerHttpRequest serverRequest);

	@Operation(summary = "디바이스 토큰 삭제", description = "등록된 디바이스 토큰을 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "토큰 삭제 성공")
	})
	@DeleteMapping("/devices/token")
	Mono<ResponseEntity<BaseResponse>> deleteDeviceToken(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			@RequestBody DeleteDeviceTokenRequest request,
			ServerHttpRequest serverRequest);

	// ==================== 사용자 동의 API ====================

	@Operation(summary = "사용자 동의 정보 조회", description = "알림 수신 동의 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/consents")
	Mono<ResponseEntity<BaseResponse>> getUserConsent(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			ServerHttpRequest serverRequest);

	@Operation(summary = "야간 광고 동의 변경", description = "야간 광고 알림 수신 동의를 변경합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@PutMapping("/consents/night-ad")
	Mono<ResponseEntity<BaseResponse>> updateNightAdConsent(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			@RequestBody UpdateNightAdConsentRequest request,
			ServerHttpRequest serverRequest);
}
