package com.study.api_gateway.util.cache;

import com.study.api_gateway.dto.profile.response.BatchUserSummaryResponse;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * Abstraction for profile cache to support future Redis integration.
 * For now, a Noop implementation will behave as cache-miss for all keys.
 */
public interface ProfileCache {
	/**
	 * Fetch cached profile summaries for the given userIds.
	 * Should return a map of userId -> summary for the entries found in cache.
	 * If none found, return an empty map (never null).
	 */
	Mono<Map<String, BatchUserSummaryResponse>> getAll(Collection<String> userIds);
	
	/**
	 * Store profile summaries into cache. Implementations may ignore TTL for now.
	 */
	Mono<Void> putAll(Map<String, BatchUserSummaryResponse> profiles);
}
