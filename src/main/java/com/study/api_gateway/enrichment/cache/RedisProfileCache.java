package com.study.api_gateway.enrichment.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.api_gateway.api.profile.dto.response.BatchUserSummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * Reactive Redis 기반 ProfileCache 구현
 * Key 형식: "profile:summary:{userId}"
 * <p>
 * ProfileCache 추상화(getAll/putAll)에 맞춰 구현하여, 유틸에서 캐시 선조회 후 미스만 원격 호출하도록 지원합니다.
 */
@Slf4j
@Component
@Primary
@ConditionalOnBean(ReactiveRedisTemplate.class)
@ConditionalOnProperty(name = "app.profile.cache.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisProfileCache implements ProfileCache {
	
	private static final String KEY_PREFIX = "profile:summary:";
	private final ReactiveRedisTemplate<String, String> redis;
	private final ObjectMapper mapper;
	private final Duration ttl; // 예: Duration.ofHours(1)
	
	public RedisProfileCache(
			@Qualifier("reactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redis,
			ObjectMapper mapper,
			@Value("${app.profile.cache.ttl:PT1H}") Duration ttl
	) {
		this.redis = redis;
		this.mapper = mapper;
		this.ttl = ttl != null ? ttl : Duration.ofHours(1);
	}
	
	private String keyFor(String userId) {
		return KEY_PREFIX + userId;
	}
	
	/**
	 * 여러 userId에 대해 mget으로 일괄 조회 후, 존재하는 항목만 역직렬화하여 맵으로 반환합니다.
	 */
	@Override
	public Mono<Map<String, BatchUserSummaryResponse>> getAll(Collection<String> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Mono.just(Map.of());
		}
		List<String> ids = new ArrayList<>();
		for (String id : userIds) {
			if (id != null && !id.isBlank()) ids.add(id);
		}
		if (ids.isEmpty()) return Mono.just(Map.of());
		
		List<String> keys = ids.stream().map(this::keyFor).toList();
		return redis.opsForValue().multiGet(keys)
				.defaultIfEmpty(Collections.emptyList())
				.map(values -> {
					if (values.size() != keys.size()) {
						log.debug("Redis multiGet size mismatch keys={} values={}", keys.size(), values.size());
					}
					Map<String, BatchUserSummaryResponse> result = new LinkedHashMap<>();
					for (int i = 0; i < ids.size(); i++) {
						String json = (values.size() > i) ? values.get(i) : null;
						if (json == null) continue;
						try {
							BatchUserSummaryResponse v = mapper.readValue(json, BatchUserSummaryResponse.class);
							if (v != null && v.getUserId() != null) {
								result.put(ids.get(i), v);
							}
						} catch (Exception e) {
							String sample = json.length() > 128 ? json.substring(0, 128) + "..." : json;
							log.warn("Failed to deserialize profile cache entry for key={}, sample={}: {}", keys.get(i), sample, e.toString());
						}
					}
					return result;
				});
	}
	
	/**
	 * putAll은 개별 set 호출로 처리(reactive에서 TTL을 유지한 멀티 set 미지원). 병렬 실행 후 완료.
	 */
	@Override
	public Mono<Void> putAll(Map<String, BatchUserSummaryResponse> profiles) {
		if (profiles == null || profiles.isEmpty()) return Mono.empty();
		List<Mono<Boolean>> ops = new ArrayList<>();
		for (Map.Entry<String, BatchUserSummaryResponse> e : profiles.entrySet()) {
			String userId = e.getKey();
			BatchUserSummaryResponse v = e.getValue();
			if (userId == null || userId.isBlank() || v == null) continue;
			try {
				String json = mapper.writeValueAsString(v);
				ops.add(redis.opsForValue().set(keyFor(userId), json, ttl));
			} catch (Exception e1) {
				log.warn("Failed to serialize profile for userId={}: {}", userId, e1.toString());
			}
		}
		if (ops.isEmpty()) return Mono.empty();
		return Mono.when(ops).then();
	}
	
	/**
	 * 단일 유저 캐시 제거
	 */
	@Override
	public Mono<Void> evict(String userId) {
		if (userId == null || userId.isBlank()) return Mono.empty();
		return redis.delete(Mono.just(keyFor(userId)))
				.doOnError(e -> log.warn("Failed to evict profile cache for userId={}: {}", userId, e.toString()))
				.then();
	}
	
	/**
	 * 여러 유저 캐시 일괄 제거
	 */
	@Override
	public Mono<Void> evictAll(Collection<String> userIds) {
		if (userIds == null || userIds.isEmpty()) return Mono.empty();
		List<String> keys = new ArrayList<>();
		for (String id : userIds) {
			if (id != null && !id.isBlank()) keys.add(keyFor(id));
		}
		if (keys.isEmpty()) return Mono.empty();
		return redis.delete(Flux.fromIterable(keys))
				.doOnError(e -> log.warn("Failed to evict multiple profile caches size={}: {}", keys.size(), e.toString()))
				.then();
	}
}
