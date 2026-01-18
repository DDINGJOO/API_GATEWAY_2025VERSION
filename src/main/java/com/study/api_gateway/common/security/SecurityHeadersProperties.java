package com.study.api_gateway.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 보안 헤더 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.headers")
public class SecurityHeadersProperties {
	
	/**
	 * 보안 헤더 필터 활성화 여부
	 */
	private boolean enabled = true;
	
	/**
	 * HSTS(HTTP Strict Transport Security) 활성화 여부
	 * HTTPS 환경에서만 true로 설정
	 */
	private boolean hstsEnabled = false;
	
	/**
	 * HSTS max-age 값 (초 단위)
	 * 기본값: 1년 (31536000초)
	 */
	private long hstsMaxAge = 31536000;
	
	/**
	 * HSTS에 서브도메인 포함 여부
	 */
	private boolean hstsIncludeSubDomains = true;
	
	/**
	 * Content-Security-Policy 값
	 */
	private String contentSecurityPolicy = "default-src 'self'; frame-ancestors 'none'";
	
	/**
	 * Referrer-Policy 값
	 */
	private String referrerPolicy = "strict-origin-when-cross-origin";
	
	/**
	 * Permissions-Policy 값
	 */
	private String permissionsPolicy = "camera=(), microphone=(), geolocation=(), payment=()";
	
	/**
	 * X-Frame-Options 값 (DENY, SAMEORIGIN, ALLOW-FROM uri)
	 */
	private String frameOptions = "DENY";
	
	/**
	 * 캐시 제어 비활성화 여부
	 * true: 캐싱 방지 헤더 추가
	 */
	private boolean noCacheEnabled = true;
}
