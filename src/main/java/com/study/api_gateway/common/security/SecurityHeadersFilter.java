package com.study.api_gateway.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 보안 헤더를 응답에 추가하는 WebFilter
 * OWASP 권장 보안 헤더를 자동으로 추가합니다.
 * <p>
 * 적용되는 보안 헤더:
 * - X-XSS-Protection: XSS 공격 방지
 * - X-Frame-Options: Clickjacking 방지
 * - X-Content-Type-Options: MIME 스니핑 방지
 * - Referrer-Policy: Referrer 정보 제한
 * - Permissions-Policy: 브라우저 기능 제한
 * - Content-Security-Policy: 리소스 로딩 제한
 * - Cache-Control: 캐싱 방지 (선택)
 * - Strict-Transport-Security: HTTPS 강제 (선택)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@ConditionalOnProperty(name = "security.headers.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityHeadersFilter implements WebFilter {
	
	private final SecurityHeadersProperties properties;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return chain.filter(exchange)
				.doOnSuccess(aVoid -> addSecurityHeaders(exchange.getResponse()));
	}
	
	private void addSecurityHeaders(ServerHttpResponse response) {
		HttpHeaders headers = response.getHeaders();
		
		// XSS 공격 방지
		headers.addIfAbsent("X-XSS-Protection", "1; mode=block");
		
		// Clickjacking 방지
		headers.addIfAbsent("X-Frame-Options", properties.getFrameOptions());
		
		// MIME 타입 스니핑 방지
		headers.addIfAbsent("X-Content-Type-Options", "nosniff");
		
		// Referrer 정보 제한
		headers.addIfAbsent("Referrer-Policy", properties.getReferrerPolicy());
		
		// 권한 정책 설정
		headers.addIfAbsent("Permissions-Policy", properties.getPermissionsPolicy());
		
		// Content Security Policy
		headers.addIfAbsent("Content-Security-Policy", properties.getContentSecurityPolicy());
		
		// 캐시 제어 (민감한 데이터 캐싱 방지)
		if (properties.isNoCacheEnabled()) {
			headers.addIfAbsent("Cache-Control", "no-store, no-cache, must-revalidate, private");
			headers.addIfAbsent("Pragma", "no-cache");
			headers.addIfAbsent("Expires", "0");
		}
		
		// HTTPS 강제 (HSTS)
		if (properties.isHstsEnabled()) {
			String hstsValue = String.format("max-age=%d%s",
					properties.getHstsMaxAge(),
					properties.isHstsIncludeSubDomains() ? "; includeSubDomains" : "");
			headers.addIfAbsent("Strict-Transport-Security", hstsValue);
		}
	}
}
