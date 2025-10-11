package com.study.api_gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.api_gateway.util.cache.ProfileCache;
import com.study.api_gateway.util.cache.RedisProfileCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Duration;

/**
 * 프로파일 캐시 빈 구성
 * - Redis 관련 빈(ReactiveRedisTemplate)이 등록되어 있을 때만 Redis 기반 ProfileCache를 등록합니다.
 * - 그렇지 않으면 NoopProfileCache(@ConditionalOnMissingBean) 가 자동 활성화됩니다.
 */
@Configuration
public class CustomConfig {
	/**
	 * Redis 기반 ProfileCache 빈 등록
	 * - TTL은 운영 환경에 맞춰 조정하세요.
	 */
	@Bean
	@Primary
	@ConditionalOnBean(name = "reactiveRedisTemplate")
	public ProfileCache profileCache(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis, ObjectMapper mapper) {
		return new RedisProfileCache(redis, mapper, Duration.ofHours(1));
	}
}
