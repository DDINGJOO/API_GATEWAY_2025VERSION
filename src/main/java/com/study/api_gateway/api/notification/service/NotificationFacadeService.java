package com.study.api_gateway.api.notification.service;

import com.study.api_gateway.api.notification.client.NotificationClient;
import com.study.api_gateway.api.notification.dto.request.DeleteDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.RegisterDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.UpdateNightAdConsentRequest;
import com.study.api_gateway.api.notification.dto.response.UserConsentResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Notification 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class NotificationFacadeService {
	
	private static final String SERVICE_NAME = "notification-service";
	private final NotificationClient notificationClient;
	private final ResilienceOperator resilience;
	
	// ==================== 디바이스 토큰 API ====================
	
	public Mono<Void> registerDeviceToken(RegisterDeviceTokenRequest request) {
		return notificationClient.registerDeviceToken(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> deleteDeviceToken(DeleteDeviceTokenRequest request) {
		return notificationClient.deleteDeviceToken(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 사용자 동의 API ====================
	
	public Mono<UserConsentResponse> getUserConsent(String userId) {
		return notificationClient.getUserConsent(userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<UserConsentResponse> updateNightAdConsent(String userId, UpdateNightAdConsentRequest request) {
		return notificationClient.updateNightAdConsent(userId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
