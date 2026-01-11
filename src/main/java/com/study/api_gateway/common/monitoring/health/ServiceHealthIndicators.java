package com.study.api_gateway.common.monitoring.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 외부 서비스별 Health Indicator Bean 설정
 */
@Configuration
public class ServiceHealthIndicators {

	private static final String DEFAULT_HEALTH_PATH = "/actuator/health";

	// ==================== Auth Service ====================
	@Bean
	public ReactiveHealthIndicator authServiceHealth(@Qualifier("authWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "auth-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Profile Service ====================
	@Bean
	public ReactiveHealthIndicator profileServiceHealth(@Qualifier("profileWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "profile-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Article Service ====================
	@Bean
	public ReactiveHealthIndicator articleServiceHealth(@Qualifier("articleWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "article-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Comment Service ====================
	@Bean
	public ReactiveHealthIndicator commentServiceHealth(@Qualifier("commentWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "comment-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Image Service ====================
	@Bean
	public ReactiveHealthIndicator imageServiceHealth(@Qualifier("imageWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "image-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Gaechu (Like) Service ====================
	@Bean
	public ReactiveHealthIndicator gaechuServiceHealth(@Qualifier("gaechuWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "gaechu-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Activity Service ====================
	@Bean
	public ReactiveHealthIndicator activityServiceHealth(@Qualifier("activitiesClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "activity-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Support Service ====================
	@Bean
	public ReactiveHealthIndicator supportServiceHealth(@Qualifier("supportWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "support-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Place Info Service ====================
	@Bean
	public ReactiveHealthIndicator placeServiceHealth(@Qualifier("placeInfoWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "place-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Room Service ====================
	@Bean
	public ReactiveHealthIndicator roomServiceHealth(@Qualifier("roomWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "room-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== YeYakHaeYo (Reservation) Service ====================
	@Bean
	public ReactiveHealthIndicator yeYakHaeYoServiceHealth(@Qualifier("yeYakHaeYoWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "yeyakhaeyo-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Room Reservation Service ====================
	@Bean
	public ReactiveHealthIndicator roomReservationServiceHealth(@Qualifier("roomReservationWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "room-reservation-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== YeYak Manage Service ====================
	@Bean
	public ReactiveHealthIndicator yeYakManageServiceHealth(@Qualifier("yeYakManageWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "yeyak-manage-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Coupon Service ====================
	@Bean
	public ReactiveHealthIndicator couponServiceHealth(@Qualifier("couponWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "coupon-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Chat Service ====================
	@Bean
	public ReactiveHealthIndicator chatServiceHealth(@Qualifier("chatWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "chat-service", DEFAULT_HEALTH_PATH) {};
	}

	// ==================== Notification Service ====================
	@Bean
	public ReactiveHealthIndicator notificationServiceHealth(@Qualifier("notificationWebClient") WebClient webClient) {
		return new ExternalServiceHealthIndicator(webClient, "notification-service", DEFAULT_HEALTH_PATH) {};
	}
}
