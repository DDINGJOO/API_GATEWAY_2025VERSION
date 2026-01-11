package com.study.api_gateway.common.ratelimit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.api_gateway.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Rate Limiting WebFilter
 * 모든 요청에 대해 Rate Limit 체크
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements WebFilter, Ordered {

	private final RateLimitService rateLimitService;
	private final RateLimitProperties properties;
	private final ObjectMapper objectMapper;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 필터 순서 - JWT 필터보다 먼저 실행 (높은 우선순위)
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		// Rate Limiting 비활성화 상태면 바로 통과
		if (!properties.isEnabled()) {
			return chain.filter(exchange);
		}

		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();

		// 제외 경로 체크
		if (isExcludedPath(path)) {
			return chain.filter(exchange);
		}

		// 사용자 식별
		String userId = request.getHeaders().getFirst("X-User-Id");
		RateLimitService.RateLimitResult result;

		if (userId != null && !userId.isEmpty()) {
			// 인증된 사용자
			result = rateLimitService.checkRateLimit(userId);
		} else {
			// 비인증 사용자 (IP 기반)
			String clientIp = extractClientIp(request);
			result = rateLimitService.checkRateLimitByIp(clientIp);
		}

		// Rate Limit 헤더 추가
		addRateLimitHeaders(exchange.getResponse(), result);

		if (!result.allowed()) {
			return handleRateLimitExceeded(exchange, result);
		}

		return chain.filter(exchange);
	}

	/**
	 * 제외 경로 체크
	 */
	private boolean isExcludedPath(String path) {
		for (String pattern : properties.getExcludePaths()) {
			if (pathMatcher.match(pattern, path)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 클라이언트 IP 추출
	 */
	private String extractClientIp(ServerHttpRequest request) {
		// X-Forwarded-For 헤더 확인 (프록시/로드밸런서 뒤에 있는 경우)
		String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			// 첫 번째 IP가 실제 클라이언트 IP
			return xForwardedFor.split(",")[0].trim();
		}

		// X-Real-IP 헤더 확인
		String xRealIp = request.getHeaders().getFirst("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty()) {
			return xRealIp;
		}

		// 직접 연결된 클라이언트 IP
		if (request.getRemoteAddress() != null) {
			return request.getRemoteAddress().getAddress().getHostAddress();
		}

		return "unknown";
	}

	/**
	 * Rate Limit 헤더 추가
	 */
	private void addRateLimitHeaders(ServerHttpResponse response, RateLimitService.RateLimitResult result) {
		HttpHeaders headers = response.getHeaders();
		headers.set("X-RateLimit-Limit", String.valueOf(result.limit()));
		headers.set("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));
		headers.set("X-RateLimit-Reset", String.valueOf(result.resetTimeSeconds()));

		if (!result.allowed()) {
			headers.set("Retry-After", String.valueOf(result.waitTimeMillis() / 1000 + 1));
		}
	}

	/**
	 * Rate Limit 초과 응답 처리
	 */
	private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, RateLimitService.RateLimitResult result) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		ErrorCode errorCode = ErrorCode.RATE_LIMIT_EXCEEDED;
		Map<String, Object> errorBody = Map.of(
				"isSuccess", false,
				"code", HttpStatus.TOO_MANY_REQUESTS.value(),
				"errorCode", errorCode.getCode(),
				"data", String.format("Rate limit exceeded. Please retry after %d seconds.",
						result.waitTimeMillis() / 1000 + 1),
				"request", Map.of(
						"path", exchange.getRequest().getURI().getPath()
				)
		);

		try {
			String json = objectMapper.writeValueAsString(errorBody);
			DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
			return response.writeWith(Mono.just(buffer));
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize rate limit response", e);
			return response.setComplete();
		}
	}
}
