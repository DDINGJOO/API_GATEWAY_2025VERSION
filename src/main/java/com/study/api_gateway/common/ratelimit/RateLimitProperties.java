package com.study.api_gateway.common.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Rate Limit 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
	
	/**
	 * Rate Limiting 활성화 여부
	 */
	private boolean enabled = true;
	
	/**
	 * 기본 설정 - 인증된 사용자
	 */
	private BucketConfig authenticated = new BucketConfig(100, 60); // 100 requests per minute
	
	/**
	 * 기본 설정 - 비인증 사용자 (IP 기반)
	 */
	private BucketConfig anonymous = new BucketConfig(30, 60); // 30 requests per minute
	
	/**
	 * 엔드포인트별 커스텀 설정
	 */
	private Map<String, BucketConfig> endpoints = new HashMap<>();
	
	/**
	 * Rate Limit 제외 경로 패턴
	 */
	private String[] excludePaths = {
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/actuator/**",
			"/health"
	};
	
	@Getter
	@Setter
	public static class BucketConfig {
		/**
		 * 버킷 용량 (최대 토큰 수)
		 */
		private int capacity;
		
		/**
		 * 리필 주기 (초)
		 */
		private int refillSeconds;
		
		/**
		 * 리필 시 추가되는 토큰 수 (기본값: capacity와 동일)
		 */
		private int refillTokens;
		
		public BucketConfig() {
			this.capacity = 100;
			this.refillSeconds = 60;
			this.refillTokens = 100;
		}
		
		public BucketConfig(int capacity, int refillSeconds) {
			this.capacity = capacity;
			this.refillSeconds = refillSeconds;
			this.refillTokens = capacity;
		}
		
		public int getRefillTokens() {
			return refillTokens > 0 ? refillTokens : capacity;
		}
	}
}
