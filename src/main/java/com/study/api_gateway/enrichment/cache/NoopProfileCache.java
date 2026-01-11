package com.study.api_gateway.enrichment.cache;

import com.study.api_gateway.api.profile.dto.response.BatchUserSummaryResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * No-op cache implementation used before Redis integration.
 * Always returns empty results and ignores put operations.
 * <p>
 * Always registered; when RedisProfileCache is available it will be marked @Primary
 * so injections will prefer Redis-backed cache. Otherwise this no-op bean will be used.
 */
@Component
public class NoopProfileCache implements ProfileCache {
	@Override
	public Mono<Map<String, BatchUserSummaryResponse>> getAll(Collection<String> userIds) {
		return Mono.just(java.util.Map.of());
	}
	
	@Override
	public Mono<Void> putAll(Map<String, BatchUserSummaryResponse> profiles) {
		return Mono.empty();
	}
	
	@Override
	public Mono<Void> evict(String userId) {
		return Mono.empty();
	}
	
	@Override
	public Mono<Void> evictAll(Collection<String> userIds) {
		return Mono.empty();
	}
}
