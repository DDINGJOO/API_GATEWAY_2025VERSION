package com.study.api_gateway.common.monitoring.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 요청 메트릭을 자동으로 수집하는 WebFilter
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnBean(MeterRegistry.class)
public class MetricsWebFilter implements WebFilter {
	
	// 메트릭 수집 제외 경로
	private static final String[] EXCLUDE_PATHS = {
			"/actuator",
			"/swagger-ui",
			"/v3/api-docs",
			"/health",
			"/favicon.ico"
	};
	private final ApiGatewayMetrics metrics;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String path = exchange.getRequest().getPath().value();
		
		// 제외 경로 체크
		if (shouldExclude(path)) {
			return chain.filter(exchange);
		}
		
		// 메트릭 수집 시작
		Timer.Sample sample = metrics.startTimer();
		metrics.incrementActiveRequests();
		
		String method = exchange.getRequest().getMethod().name();
		String endpoint = normalizeEndpoint(path);
		
		return chain.filter(exchange)
				.doFinally(signalType -> {
					// 요청 종료 시 메트릭 기록
					metrics.decrementActiveRequests();
					
					HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
					String status = statusCode != null ? String.valueOf(statusCode.value()) : "unknown";
					String statusCategory = getStatusCategory(statusCode);
					
					// 요청 카운트 기록
					metrics.incrementRequestCount(endpoint, method, statusCategory);
					
					// 응답 시간 기록
					metrics.recordRequestDuration(sample, endpoint, method, statusCategory);
					
					// 에러 발생 시 에러 카운트 기록
					if (statusCode != null && statusCode.isError()) {
						metrics.incrementErrorCount(endpoint, statusCategory);
					}
				});
	}
	
	/**
	 * 경로 정규화 (Path Variable 제거)
	 */
	private String normalizeEndpoint(String path) {
		// UUID 패턴 정규화
		String normalized = path.replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "{id}");
		
		// 숫자 ID 패턴 정규화
		normalized = normalized.replaceAll("/\\d+", "/{id}");
		
		// 이메일 패턴 정규화
		normalized = normalized.replaceAll("/[^/]+@[^/]+\\.[^/]+", "/{email}");
		
		return normalized;
	}
	
	/**
	 * 상태 코드 카테고리 반환
	 */
	private String getStatusCategory(HttpStatusCode statusCode) {
		if (statusCode == null) {
			return "unknown";
		}
		
		int code = statusCode.value();
		if (code >= 200 && code < 300) {
			return "2xx";
		} else if (code >= 300 && code < 400) {
			return "3xx";
		} else if (code >= 400 && code < 500) {
			return "4xx";
		} else if (code >= 500) {
			return "5xx";
		}
		return "unknown";
	}
	
	/**
	 * 제외 경로 체크
	 */
	private boolean shouldExclude(String path) {
		for (String excludePath : EXCLUDE_PATHS) {
			if (path.startsWith(excludePath)) {
				return true;
			}
		}
		return false;
	}
}
