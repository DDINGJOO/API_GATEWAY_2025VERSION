package com.study.api_gateway.api.notification.controller;

import com.study.api_gateway.api.notification.dto.request.DeleteDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.RegisterDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.UpdateNightAdConsentRequest;
import com.study.api_gateway.api.notification.service.NotificationFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import io.swagger.v3.oas.annotations.Parameter;
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
public class NotificationController implements NotificationApi {
	
	private final NotificationFacadeService notificationFacadeService;
	private final ResponseFactory responseFactory;
	
	@Override
	@PostMapping("/devices/token")
	public Mono<ResponseEntity<BaseResponse>> registerDeviceToken(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			@RequestBody RegisterDeviceTokenRequest request,
			ServerHttpRequest serverRequest
	) {
		log.debug("registerDeviceToken: userId={}, platform={}", userId, request.getPlatform());
		
		request.setUserId(userId);
		
		return notificationFacadeService.registerDeviceToken(request)
				.then(Mono.fromCallable(() -> responseFactory.ok(null, serverRequest, HttpStatus.CREATED)));
	}
	
	@Override
	@DeleteMapping("/devices/token")
	public Mono<ResponseEntity<BaseResponse>> deleteDeviceToken(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			@RequestBody DeleteDeviceTokenRequest request,
			ServerHttpRequest serverRequest
	) {
		log.debug("deleteDeviceToken: userId={}", userId);
		
		request.setUserId(userId);
		
		return notificationFacadeService.deleteDeviceToken(request)
				.then(Mono.fromCallable(() -> responseFactory.ok(null, serverRequest, HttpStatus.NO_CONTENT)));
	}
	
	@Override
	@GetMapping("/consents")
	public Mono<ResponseEntity<BaseResponse>> getUserConsent(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			ServerHttpRequest serverRequest
	) {
		log.debug("getUserConsent: userId={}", userId);
		
		return notificationFacadeService.getUserConsent(String.valueOf(userId))
				.map(result -> responseFactory.ok(result, serverRequest));
	}
	
	@Override
	@PutMapping("/consents/night-ad")
	public Mono<ResponseEntity<BaseResponse>> updateNightAdConsent(
			@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
			@RequestBody UpdateNightAdConsentRequest request,
			ServerHttpRequest serverRequest
	) {
		log.debug("updateNightAdConsent: userId={}, consented={}", userId, request.getConsented());
		
		return notificationFacadeService.updateNightAdConsent(String.valueOf(userId), request)
				.map(result -> responseFactory.ok(result, serverRequest));
	}
}
