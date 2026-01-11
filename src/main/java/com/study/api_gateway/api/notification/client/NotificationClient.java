package com.study.api_gateway.api.notification.client;

import com.study.api_gateway.api.notification.dto.request.DeleteDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.RegisterDeviceTokenRequest;
import com.study.api_gateway.api.notification.dto.request.UpdateNightAdConsentRequest;
import com.study.api_gateway.api.notification.dto.response.UserConsentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class NotificationClient {
	private final WebClient webClient;
	private static final String DEVICES_PREFIX = "/api/v1/devices";
	private static final String CONSENTS_PREFIX = "/api/v1/consents";

	public NotificationClient(@Qualifier(value = "notificationWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	// ==================== 디바이스 토큰 API ====================

	/**
	 * 디바이스 토큰 등록
	 * POST /api/v1/devices/token
	 */
	public Mono<Void> registerDeviceToken(RegisterDeviceTokenRequest request) {
		String uriString = UriComponentsBuilder.fromPath(DEVICES_PREFIX + "/token")
				.toUriString();

		log.debug("registerDeviceToken: userId={}, platform={}", request.getUserId(), request.getPlatform());

		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.onStatus(HttpStatusCode::isError, response ->
						response.bodyToMono(String.class)
								.flatMap(body -> Mono.error(new RuntimeException("Failed to register device token: " + body))))
				.bodyToMono(Void.class);
	}

	/**
	 * 디바이스 토큰 삭제
	 * DELETE /api/v1/devices/token
	 */
	public Mono<Void> deleteDeviceToken(DeleteDeviceTokenRequest request) {
		String uriString = UriComponentsBuilder.fromPath(DEVICES_PREFIX + "/token")
				.toUriString();

		log.debug("deleteDeviceToken: userId={}", request.getUserId());

		return webClient.method(org.springframework.http.HttpMethod.DELETE)
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.onStatus(HttpStatusCode::isError, response ->
						response.bodyToMono(String.class)
								.flatMap(body -> Mono.error(new RuntimeException("Failed to delete device token: " + body))))
				.bodyToMono(Void.class);
	}

	// ==================== 사용자 동의 API ====================

	/**
	 * 사용자 동의 정보 조회
	 * GET /api/v1/consents/{userId}
	 */
	public Mono<UserConsentResponse> getUserConsent(String userId) {
		String uriString = UriComponentsBuilder.fromPath(CONSENTS_PREFIX + "/{userId}")
				.buildAndExpand(userId)
				.toUriString();

		log.debug("getUserConsent: userId={}", userId);

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractUserConsentResponse);
	}

	/**
	 * 야간 광고 동의 변경
	 * PUT /api/v1/consents/{userId}/night-ad
	 */
	public Mono<UserConsentResponse> updateNightAdConsent(String userId, UpdateNightAdConsentRequest request) {
		String uriString = UriComponentsBuilder.fromPath(CONSENTS_PREFIX + "/{userId}/night-ad")
				.buildAndExpand(userId)
				.toUriString();

		log.debug("updateNightAdConsent: userId={}, consented={}", userId, request.getConsented());

		return webClient.put()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.map(this::extractUserConsentResponse);
	}

	// ==================== Response 변환 헬퍼 ====================

	@SuppressWarnings("unchecked")
	private UserConsentResponse extractUserConsentResponse(Map<String, Object> response) {
		Map<String, Object> data = (Map<String, Object>) response.get("data");
		if (data == null) {
			data = response;
		}

		return UserConsentResponse.builder()
				.userId((String) data.get("userId"))
				.serviceConsent((Boolean) data.get("serviceConsent"))
				.marketingConsent((Boolean) data.get("marketingConsent"))
				.nightAdConsent((Boolean) data.get("nightAdConsent"))
				.updatedAt(parseDateTime(data.get("updatedAt")))
				.build();
	}

	private LocalDateTime parseDateTime(Object value) {
		if (value == null) return null;
		if (value instanceof LocalDateTime) return (LocalDateTime) value;
		if (value instanceof String) {
			try {
				return LocalDateTime.parse((String) value);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
