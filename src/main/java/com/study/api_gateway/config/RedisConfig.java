package com.study.api_gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	@Bean
	public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
		// application.yaml의 spring.data.redis.host/port를 자동으로 사용
		return new LettuceConnectionFactory();
	}
	
	@Bean
	public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
			ReactiveRedisConnectionFactory connectionFactory) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		StringRedisSerializer valueSerializer = new StringRedisSerializer(); // value는 JSON 문자열로 저장
		
		RedisSerializationContext.RedisSerializationContextBuilder<String, String> builder =
				RedisSerializationContext.newSerializationContext(keySerializer);
		
		RedisSerializationContext<String, String> context = builder.value(valueSerializer).build();
		return new ReactiveRedisTemplate<>(connectionFactory, context);
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
	
}
