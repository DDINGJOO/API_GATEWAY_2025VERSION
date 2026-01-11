package com.study.api_gateway.enrichment;

import com.study.api_gateway.api.room.client.RoomClient;
import com.study.api_gateway.api.room.dto.response.RoomDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Room 정보 캐싱 서비스
 * 개별 조회와 배치 조회 모두 캐싱하여 중복 요청 최소화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomCacheService {
	
	private static final String CACHE_NAME = "roomCache";
	private final RoomClient roomClient;
	private final CacheManager roomCacheManager;
	
	/**
	 * 개별 Room 조회 (캐싱 적용)
	 *
	 * @param roomId 조회할 Room ID
	 * @return Room 정보
	 */
	@Cacheable(value = CACHE_NAME, key = "#roomId", cacheManager = "roomCacheManager")
	public Mono<RoomDetailResponse> getRoomById(Long roomId) {
		log.debug("Cache miss - fetching room from server: roomId={}", roomId);
		return roomClient.getRoomById(roomId)
				.doOnNext(room -> log.debug("Room fetched and cached: roomId={}", roomId));
	}
	
	/**
	 * 배치 Room 조회 (스마트 캐싱 적용)
	 * 캐시된 항목과 캐시되지 않은 항목을 분리하여 처리
	 *
	 * @param roomIds 조회할 Room ID 목록
	 * @return Room ID를 키로 하는 Map
	 */
	public Mono<Map<Long, RoomDetailResponse>> getRoomsByBatchWithCache(List<Long> roomIds) {
		if (roomIds == null || roomIds.isEmpty()) {
			return Mono.just(Map.of());
		}
		
		// 중복 제거
		List<Long> uniqueRoomIds = roomIds.stream()
				.distinct()
				.sorted()
				.collect(Collectors.toList());
		
		log.info("Processing batch request for {} unique room IDs", uniqueRoomIds.size());
		
		// 캐시 확인
		Cache cache = roomCacheManager.getCache(CACHE_NAME);
		if (cache == null) {
			log.warn("Cache not available, fetching all from server");
			return fetchFromServer(uniqueRoomIds);
		}
		
		// 캐시된 항목과 캐시되지 않은 항목 분리
		Map<Long, RoomDetailResponse> cachedResults = new HashMap<>();
		List<Long> uncachedIds = new ArrayList<>();
		
		for (Long roomId : uniqueRoomIds) {
			RoomDetailResponse cached = cache.get(roomId, RoomDetailResponse.class);
			if (cached != null) {
				cachedResults.put(roomId, cached);
				log.debug("Cache hit for roomId: {}", roomId);
			} else {
				uncachedIds.add(roomId);
				log.debug("Cache miss for roomId: {}", roomId);
			}
		}
		
		log.info("Cache status - hits: {}, misses: {}", cachedResults.size(), uncachedIds.size());
		
		// 캐시되지 않은 항목이 없으면 캐시된 결과만 반환
		if (uncachedIds.isEmpty()) {
			return Mono.just(cachedResults);
		}
		
		// 캐시되지 않은 항목만 서버에서 조회
		return fetchFromServer(uncachedIds)
				.map(fetchedRooms -> {
					// 새로 조회한 결과를 캐시에 저장
					fetchedRooms.forEach((roomId, room) -> {
						cache.put(roomId, room);
						log.debug("Cached new room: roomId={}", roomId);
					});
					
					// 캐시된 결과와 새로 조회한 결과 병합
					Map<Long, RoomDetailResponse> allResults = new HashMap<>(cachedResults);
					allResults.putAll(fetchedRooms);
					
					return allResults;
				});
	}
	
	/**
	 * 서버에서 Room 정보 배치 조회
	 */
	private Mono<Map<Long, RoomDetailResponse>> fetchFromServer(List<Long> roomIds) {
		log.info("Fetching {} rooms from server", roomIds.size());
		return roomClient.getRoomsByIds(roomIds)
				.map(rooms -> {
					Map<Long, RoomDetailResponse> roomMap = rooms.stream()
							.collect(Collectors.toMap(
									RoomDetailResponse::getRoomId,
									Function.identity(),
									(existing, replacement) -> existing
							));
					log.info("Fetched {} rooms from server", roomMap.size());
					return roomMap;
				})
				.onErrorResume(error -> {
					log.error("Room 배치 조회 실패: roomIds={}, error={}", roomIds, error.getMessage());
					return Mono.just(Map.of());
				});
	}
	
	/**
	 * 캐시 통계 조회
	 */
	public Map<String, Object> getCacheStats() {
		Map<String, Object> stats = new HashMap<>();
		Cache cache = roomCacheManager.getCache(CACHE_NAME);
		
		if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
			com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache =
					(com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
			
			stats.put("size", caffeineCache.estimatedSize());
			stats.put("stats", caffeineCache.stats().toString());
		}
		
		return stats;
	}
	
	/**
	 * 캐시 무효화 (특정 Room)
	 */
	public void evictRoom(Long roomId) {
		Cache cache = roomCacheManager.getCache(CACHE_NAME);
		if (cache != null) {
			cache.evict(roomId);
			log.info("Evicted room from cache: roomId={}", roomId);
		}
	}
	
	/**
	 * 전체 캐시 클리어
	 */
	public void clearAllCache() {
		Cache cache = roomCacheManager.getCache(CACHE_NAME);
		if (cache != null) {
			cache.clear();
			log.info("Cleared all room cache");
		}
	}
}
