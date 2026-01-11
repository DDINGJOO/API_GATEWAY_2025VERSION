package com.study.api_gateway.common.resilience;

import com.study.api_gateway.common.exception.ErrorCode;
import com.study.api_gateway.common.exception.GatewayException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Circuit Breaker 및 서비스 호출 실패 시 Fallback 처리
 */
@Slf4j
@Component
public class FallbackHandler {

	/**
	 * Mono 에러 핸들링
	 */
	public <T> Mono<T> handle(String serviceName, Throwable throwable) {
		ErrorCode errorCode = mapToErrorCode(throwable);
		String message = buildErrorMessage(serviceName, throwable);

		log.error("Fallback triggered for {}: [{}] {} - {}",
				serviceName, errorCode.getCode(), errorCode.getMessage(), throwable.getMessage());

		return Mono.error(new GatewayException(errorCode, message, throwable));
	}

	/**
	 * Flux 에러 핸들링
	 */
	public <T> Flux<T> handleFlux(String serviceName, Throwable throwable) {
		return this.<T>handle(serviceName, throwable).flux();
	}

	/**
	 * 예외 타입에 따른 ErrorCode 매핑
	 */
	private ErrorCode mapToErrorCode(Throwable throwable) {
		// Circuit Breaker OPEN 상태
		if (throwable instanceof CallNotPermittedException) {
			return ErrorCode.SERVICE_UNAVAILABLE;
		}

		// Timeout
		if (throwable instanceof TimeoutException) {
			return ErrorCode.GATEWAY_TIMEOUT;
		}

		// Connection 실패
		if (throwable instanceof WebClientRequestException || throwable instanceof IOException) {
			return ErrorCode.BAD_GATEWAY;
		}

		// Backend 5xx 에러
		if (throwable instanceof WebClientResponseException ex) {
			if (ex.getStatusCode().is5xxServerError()) {
				return ErrorCode.BAD_GATEWAY;
			}
			if (ex.getStatusCode().is4xxClientError()) {
				return ErrorCode.INVALID_REQUEST;
			}
		}

		return ErrorCode.INTERNAL_ERROR;
	}

	/**
	 * 사용자 친화적 에러 메시지 생성
	 */
	private String buildErrorMessage(String serviceName, Throwable throwable) {
		if (throwable instanceof CallNotPermittedException) {
			return String.format("%s is temporarily unavailable. Please try again later.", serviceName);
		}

		if (throwable instanceof TimeoutException) {
			return String.format("%s did not respond within the expected time.", serviceName);
		}

		if (throwable instanceof WebClientRequestException) {
			return String.format("Unable to connect to %s.", serviceName);
		}

		if (throwable instanceof WebClientResponseException ex) {
			return String.format("Error response from %s: %s", serviceName, ex.getStatusCode());
		}

		return String.format("An error occurred while communicating with %s.", serviceName);
	}
}
