package com.study.api_gateway.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

/**
 * WebFlux용 Resilience Operator
 *
 * Operator Composition 패턴을 사용하여 Circuit Breaker, Timeout, Retry를 적용
 * 상속 대신 조합(Composition)을 통해 리액티브 패러다임에 맞는 구현 제공
 *
 * 사용 예시:
 * <pre>
 * return authClient.login(request)
 *     .transform(resilience.protect("auth-service"));
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResilienceOperator {

	private final CircuitBreakerRegistry circuitBreakerRegistry;
	private final RetryRegistry retryRegistry;
	private final FallbackHandler fallbackHandler;

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

	/**
	 * 기본 보호: Circuit Breaker + Timeout + Fallback
	 *
	 * @param serviceName 서비스 이름 (Circuit Breaker 인스턴스 식별자)
	 * @return Mono 변환 함수
	 */
	public <T> Function<Mono<T>, Mono<T>> protect(String serviceName) {
		return protect(serviceName, DEFAULT_TIMEOUT);
	}

	/**
	 * 커스텀 타임아웃 보호
	 *
	 * @param serviceName 서비스 이름
	 * @param timeout 타임아웃 시간
	 * @return Mono 변환 함수
	 */
	public <T> Function<Mono<T>, Mono<T>> protect(String serviceName, Duration timeout) {
		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);

		return mono -> mono
				.doOnSubscribe(s -> log.debug("Calling {} with circuit breaker", serviceName))
				.transformDeferred(CircuitBreakerOperator.of(cb))
				.timeout(timeout)
				.doOnError(e -> log.warn("Error from {}: {}", serviceName, e.getMessage()))
				.onErrorResume(t -> fallbackHandler.handle(serviceName, t));
	}

	/**
	 * Retry 포함 보호: Retry -> Circuit Breaker -> Timeout -> Fallback
	 *
	 * @param serviceName 서비스 이름
	 * @return Mono 변환 함수
	 */
	public <T> Function<Mono<T>, Mono<T>> protectWithRetry(String serviceName) {
		return protectWithRetry(serviceName, DEFAULT_TIMEOUT);
	}

	/**
	 * Retry 포함 보호 (커스텀 타임아웃)
	 *
	 * @param serviceName 서비스 이름
	 * @param timeout 타임아웃 시간
	 * @return Mono 변환 함수
	 */
	public <T> Function<Mono<T>, Mono<T>> protectWithRetry(String serviceName, Duration timeout) {
		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);
		Retry retry = retryRegistry.retry(serviceName);

		return mono -> mono
				.doOnSubscribe(s -> log.debug("Calling {} with retry and circuit breaker", serviceName))
				.transformDeferred(RetryOperator.of(retry))
				.transformDeferred(CircuitBreakerOperator.of(cb))
				.timeout(timeout)
				.doOnError(e -> log.warn("Error from {}: {}", serviceName, e.getMessage()))
				.onErrorResume(t -> fallbackHandler.handle(serviceName, t));
	}

	/**
	 * Flux용 보호
	 *
	 * @param serviceName 서비스 이름
	 * @return Flux 변환 함수
	 */
	public <T> Function<Flux<T>, Flux<T>> protectFlux(String serviceName) {
		return protectFlux(serviceName, DEFAULT_TIMEOUT);
	}

	/**
	 * Flux용 보호 (커스텀 타임아웃)
	 *
	 * @param serviceName 서비스 이름
	 * @param timeout 타임아웃 시간
	 * @return Flux 변환 함수
	 */
	public <T> Function<Flux<T>, Flux<T>> protectFlux(String serviceName, Duration timeout) {
		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(serviceName);

		return flux -> flux
				.doOnSubscribe(s -> log.debug("Calling {} (Flux) with circuit breaker", serviceName))
				.transformDeferred(CircuitBreakerOperator.of(cb))
				.timeout(timeout)
				.doOnError(e -> log.warn("Error from {}: {}", serviceName, e.getMessage()))
				.onErrorResume(t -> fallbackHandler.handleFlux(serviceName, t));
	}

	/**
	 * 보호 없이 Fallback만 적용 (기존 로직 호환용)
	 *
	 * @param serviceName 서비스 이름
	 * @return Mono 변환 함수
	 */
	public <T> Function<Mono<T>, Mono<T>> withFallbackOnly(String serviceName) {
		return mono -> mono
				.onErrorResume(t -> fallbackHandler.handle(serviceName, t));
	}
}
