package com.study.api_gateway.config;

import com.study.api_gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityFilterConfig {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	/**
	 * JWT 인증 필터를 등록합니다.
	 * Order를 -100으로 설정하여 CORS 필터보다 먼저 실행되도록 합니다.
	 * (CorsWebFilter의 기본 Order는 Ordered.HIGHEST_PRECEDENCE + 0)
	 */
	@Bean
	@Order(-100)
	public WebFilter jwtAuthenticationWebFilter() {
		return jwtAuthenticationFilter;
	}
}
