package com.study.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebFluxCorsConfig {
	
	@Value("${cors.allowed-origins:*}")
	private String allowedOrigins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
	    
	    // 환경변수로 허용 Origin 관리
	    // 개발: * (모든 origin 허용 - 앱/웹 모두 OK)
	    // 프로덕션: 특정 도메인만 허용 (웹 관리자 페이지 등)
	    List<String> origins = Arrays.asList(allowedOrigins.split(","));
	    config.setAllowedOriginPatterns(origins);
	    
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
