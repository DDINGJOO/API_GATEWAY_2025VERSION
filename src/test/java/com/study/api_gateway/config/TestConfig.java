package com.study.api_gateway.config;

import com.study.api_gateway.common.config.AuthorizationConfig;
import com.study.api_gateway.common.util.JwtTokenValidator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

	@Bean
	@Primary
	public JwtTokenValidator jwtTokenValidator() {
		return new JwtTokenValidator();
	}

	@Bean
	@Primary
	public AuthorizationConfig authorizationConfig() {
		return new AuthorizationConfig();
	}
}
