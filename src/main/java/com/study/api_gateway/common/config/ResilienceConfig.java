package com.study.api_gateway.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Resilience4j Circuit Breaker 및 Retry 설정
 */
@Configuration
public class ResilienceConfig {
	
	/**
	 * Circuit Breaker 레지스트리
	 * <p>
	 * 설정:
	 * - 슬라이딩 윈도우: COUNT_BASED, 10개 호출 기준
	 * - 최소 호출 수: 5
	 * - 실패율 임계치: 50%
	 * - 느린 호출 임계치: 80% (3초 이상)
	 * - OPEN 상태 대기: 10초
	 * - HALF_OPEN 허용 호출: 3
	 */
	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
				.slidingWindowType(SlidingWindowType.COUNT_BASED)
				.slidingWindowSize(10)
				.minimumNumberOfCalls(5)
				.failureRateThreshold(50)
				.slowCallRateThreshold(80)
				.slowCallDurationThreshold(Duration.ofSeconds(3))
				.waitDurationInOpenState(Duration.ofSeconds(10))
				.permittedNumberOfCallsInHalfOpenState(3)
				.automaticTransitionFromOpenToHalfOpenEnabled(true)
				.recordExceptions(
						IOException.class,
						TimeoutException.class,
						WebClientRequestException.class
				)
				.build();
		
		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
		
		// 서비스별 커스텀 설정 (필요시)
		registerCustomCircuitBreaker(registry, "auth-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "profile-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "article-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "place-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "room-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "reservation-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "chat-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "coupon-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "notification-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "image-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "activity-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "comment-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "gaechu-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "room-reservation-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "reservation-manage-service", defaultConfig);
		registerCustomCircuitBreaker(registry, "support-service", defaultConfig);
		
		return registry;
	}
	
	/**
	 * Retry 레지스트리
	 * <p>
	 * 설정:
	 * - 최대 시도: 3회
	 * - 대기 시간: 500ms
	 * - 재시도 대상: WebClientRequestException, IOException
	 */
	@Bean
	public RetryRegistry retryRegistry() {
		RetryConfig defaultConfig = RetryConfig.custom()
				.maxAttempts(3)
				.waitDuration(Duration.ofMillis(500))
				.retryExceptions(
						WebClientRequestException.class,
						IOException.class
				)
				.build();
		
		return RetryRegistry.of(defaultConfig);
	}
	
	private void registerCustomCircuitBreaker(
			CircuitBreakerRegistry registry,
			String name,
			CircuitBreakerConfig config) {
		registry.circuitBreaker(name, config);
	}
}
