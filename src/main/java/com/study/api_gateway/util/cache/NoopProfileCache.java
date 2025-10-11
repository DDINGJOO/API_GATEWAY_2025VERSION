package com.study.api_gateway.util.cache;

import com.study.api_gateway.dto.profile.response.BatchUserSummaryResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * No-op cache implementation used before Redis integration.
 * Always returns empty results and ignores put operations.
 * <p>
 * RedisProfileCache 빈이 구성되지 않았을 때만 활성화되도록 조건부 로딩합니다.
 */
@Component
@ConditionalOnMissingBean(ProfileCache.class)
public class NoopProfileCache implements ProfileCache {
	@Override
	public Mono<Map<String, BatchUserSummaryResponse>> getAll(Collection<String> userIds) {
		return Mono.just(java.util.Map.of());
	}
	
	@Override
	public Mono<Void> putAll(Map<String, BatchUserSummaryResponse> profiles) {
		return Mono.empty();
	}
}
