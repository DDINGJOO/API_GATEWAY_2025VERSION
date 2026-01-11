package com.study.api_gateway.common.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API Gateway 커스텀 메트릭
 */
@Slf4j
@Component
@ConditionalOnBean(MeterRegistry.class)
public class ApiGatewayMetrics {

	private final MeterRegistry meterRegistry;

	// 캐시된 메트릭을 위한 Map
	private final ConcurrentHashMap<String, Counter> requestCounters = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Counter> errorCounters = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Timer> requestTimers = new ConcurrentHashMap<>();

	// Rate Limiting 메트릭
	private final Counter rateLimitedRequests;
	private final AtomicLong activeRequests;

	// 인증 메트릭
	private final Counter authSuccessCounter;
	private final Counter authFailureCounter;

	// 캐시 메트릭
	private final Counter cacheHitCounter;
	private final Counter cacheMissCounter;

	public ApiGatewayMetrics(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;

		// Rate Limiting 메트릭 초기화
		this.rateLimitedRequests = Counter.builder("api_gateway_rate_limited_requests_total")
				.description("Total number of rate limited requests")
				.register(meterRegistry);

		// Active Requests 게이지
		this.activeRequests = new AtomicLong(0);
		meterRegistry.gauge("api_gateway_active_requests", activeRequests);

		// 인증 메트릭 초기화
		this.authSuccessCounter = Counter.builder("api_gateway_auth_success_total")
				.description("Total number of successful authentications")
				.register(meterRegistry);

		this.authFailureCounter = Counter.builder("api_gateway_auth_failure_total")
				.description("Total number of failed authentications")
				.register(meterRegistry);

		// 캐시 메트릭 초기화
		this.cacheHitCounter = Counter.builder("api_gateway_cache_hits_total")
				.description("Total number of cache hits")
				.register(meterRegistry);

		this.cacheMissCounter = Counter.builder("api_gateway_cache_misses_total")
				.description("Total number of cache misses")
				.register(meterRegistry);

		log.info("[Metrics] API Gateway metrics initialized");
	}

	// ==================== Request Metrics ====================

	/**
	 * 요청 카운터 증가
	 */
	public void incrementRequestCount(String endpoint, String method, String status) {
		String key = endpoint + "_" + method + "_" + status;
		requestCounters.computeIfAbsent(key, k ->
				Counter.builder("api_gateway_requests_total")
						.description("Total API Gateway requests")
						.tag("endpoint", endpoint)
						.tag("method", method)
						.tag("status", status)
						.register(meterRegistry)
		).increment();
	}

	/**
	 * 에러 카운터 증가
	 */
	public void incrementErrorCount(String endpoint, String errorType) {
		String key = endpoint + "_" + errorType;
		errorCounters.computeIfAbsent(key, k ->
				Counter.builder("api_gateway_errors_total")
						.description("Total API Gateway errors")
						.tag("endpoint", endpoint)
						.tag("error_type", errorType)
						.register(meterRegistry)
		).increment();
	}

	/**
	 * 요청 타이머 기록
	 */
	public Timer.Sample startTimer() {
		return Timer.start(meterRegistry);
	}

	public void recordRequestDuration(Timer.Sample sample, String endpoint, String method, String status) {
		String key = endpoint + "_" + method;
		Timer timer = requestTimers.computeIfAbsent(key, k ->
				Timer.builder("api_gateway_request_duration_seconds")
						.description("API Gateway request duration")
						.tag("endpoint", endpoint)
						.tag("method", method)
						.publishPercentileHistogram()
						.publishPercentiles(0.5, 0.75, 0.95, 0.99)
						.register(meterRegistry)
		);
		sample.stop(timer);
	}

	// ==================== Rate Limiting Metrics ====================

	/**
	 * Rate Limited 요청 카운터 증가
	 */
	public void incrementRateLimitedRequests() {
		rateLimitedRequests.increment();
	}

	// ==================== Active Requests Metrics ====================

	/**
	 * 활성 요청 증가
	 */
	public void incrementActiveRequests() {
		activeRequests.incrementAndGet();
	}

	/**
	 * 활성 요청 감소
	 */
	public void decrementActiveRequests() {
		activeRequests.decrementAndGet();
	}

	// ==================== Authentication Metrics ====================

	/**
	 * 인증 성공 카운터 증가
	 */
	public void incrementAuthSuccess() {
		authSuccessCounter.increment();
	}

	/**
	 * 인증 실패 카운터 증가
	 */
	public void incrementAuthFailure() {
		authFailureCounter.increment();
	}

	// ==================== Cache Metrics ====================

	/**
	 * 캐시 히트 카운터 증가
	 */
	public void incrementCacheHit() {
		cacheHitCounter.increment();
	}

	/**
	 * 캐시 미스 카운터 증가
	 */
	public void incrementCacheMiss() {
		cacheMissCounter.increment();
	}

	// ==================== External Service Metrics ====================

	/**
	 * 외부 서비스 호출 기록
	 */
	public void recordExternalServiceCall(String serviceName, String operation, Duration duration, boolean success) {
		Timer.builder("api_gateway_external_service_duration_seconds")
				.description("External service call duration")
				.tag("service", serviceName)
				.tag("operation", operation)
				.tag("success", String.valueOf(success))
				.publishPercentileHistogram()
				.register(meterRegistry)
				.record(duration);

		Counter.builder("api_gateway_external_service_calls_total")
				.description("Total external service calls")
				.tag("service", serviceName)
				.tag("operation", operation)
				.tag("success", String.valueOf(success))
				.register(meterRegistry)
				.increment();
	}
}
