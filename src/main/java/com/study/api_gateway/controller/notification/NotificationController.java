package com.study.api_gateway.controller.notification;

import com.study.api_gateway.client.NotificationClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.notification.request.DeleteDeviceTokenRequest;
import com.study.api_gateway.dto.notification.request.RegisterDeviceTokenRequest;
import com.study.api_gateway.dto.notification.request.UpdateNightAdConsentRequest;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/bff/v1/notification")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

	private final NotificationClient notificationClient;
	private final ResponseFactory responseFactory;

	// ==================== 디바이스 토큰 API ====================

	@Operation(summary = "디바이스 토큰 등록", description = "FCM 푸시 알림을 위한 디바이스 토큰을 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "토큰 등록 성공")
	})
	@PostMapping("/devices/token")
	public Mono<ResponseEntity<BaseResponse>> registerDeviceToken(
			@RequestHeader("X-User-Id") Long userId,
			@RequestBody RegisterDeviceTokenRequest request,
			ServerHttpRequest serverRequest
	) {
		log.debug("registerDeviceToken: userId={}, platform={}", userId, request.getPlatform());

		request.setUserId(userId);

		return notificationClient.registerDeviceToken(request)
				.then(Mono.fromCallable(() -> responseFactory.ok(null, serverRequest, HttpStatus.CREATED)));
	}

	@Operation(summary = "디바이스 토큰 삭제", description = "등록된 디바이스 토큰을 삭제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "토큰 삭제 성공")
	})
	@DeleteMapping("/devices/token")
	public Mono<ResponseEntity<BaseResponse>> deleteDeviceToken(
			@RequestHeader("X-User-Id") Long userId,
			@RequestBody DeleteDeviceTokenRequest request,
			ServerHttpRequest serverRequest
	) {
		log.debug("deleteDeviceToken: userId={}", userId);

		request.setUserId(userId);

		return notificationClient.deleteDeviceToken(request)
				.then(Mono.fromCallable(() -> responseFactory.ok(null, serverRequest, HttpStatus.NO_CONTENT)));
	}

	// ==================== 사용자 동의 API ====================

	@Operation(summary = "사용자 동의 정보 조회", description = "알림 수신 동의 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping("/consents")
	public Mono<ResponseEntity<BaseResponse>> getUserConsent(
			@RequestHeader("X-User-Id") Long userId,
			ServerHttpRequest serverRequest
	) {
		log.debug("getUserConsent: userId={}", userId);

		return notificationClient.getUserConsent(String.valueOf(userId))
				.map(result -> responseFactory.ok(result, serverRequest));
	}

	@Operation(summary = "야간 광고 동의 변경", description = "야간 광고 알림 수신 동의를 변경합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@PutMapping("/consents/night-ad")
	public Mono<ResponseEntity<BaseResponse>> updateNightAdConsent(
			@RequestHeader("X-User-Id") Long userId,
			@RequestBody UpdateNightAdConsentRequest request,
			ServerHttpRequest serverRequest
	) {
		log.debug("updateNightAdConsent: userId={}, consented={}", userId, request.getConsented());

		return notificationClient.updateNightAdConsent(String.valueOf(userId), request)
				.map(result -> responseFactory.ok(result, serverRequest));
	}
}
