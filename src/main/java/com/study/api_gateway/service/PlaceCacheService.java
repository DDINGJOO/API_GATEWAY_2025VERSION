package com.study.api_gateway.service;

import com.study.api_gateway.client.PlaceClient;
import com.study.api_gateway.dto.place.response.PlaceBatchDetailResponse;
import com.study.api_gateway.dto.place.response.PlaceInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Place 정보 캐싱 서비스
 * 개별 조회와 배치 조회 모두 캐싱하여 중복 요청 최소화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceCacheService {
	
	private final PlaceClient placeClient;
	private final CacheManager placeCacheManager;
	
	// 개별 Place 캐시를 저장할 로컬 캐시
	private final Map<Long, PlaceInfoResponse> localCache = new ConcurrentHashMap<>();
	
	/**
	 * 개별 Place 조회 (캐싱 적용)
	 *
	 * @param placeId 조회할 Place ID
	 * @return Place 정보
	 */
	@Cacheable(value = "placeCache", key = "#placeId", cacheManager = "placeCacheManager")
	public Mono<PlaceInfoResponse> getPlaceById(String placeId) {
		log.debug("Cache miss - fetching place from server: placeId={}", placeId);
		return placeClient.getPlaceById(placeId)
				.doOnNext(place -> log.debug("Place fetched and cached: placeId={}", placeId));
	}
	
	/**
	 * 배치 Place 조회 (스마트 캐싱 적용)
	 * 캐시된 항목과 캐시되지 않은 항목을 분리하여 처리
	 *
	 * @param placeIds 조회할 Place ID 목록
	 * @return 배치 조회 결과
	 */
	public Mono<PlaceBatchDetailResponse> getPlacesByBatchWithCache(List<Long> placeIds) {
		if (placeIds == null || placeIds.isEmpty()) {
			return Mono.just(PlaceBatchDetailResponse.builder()
					.results(List.of())
					.build());
		}
		
		// 중복 제거 및 정렬 (캐시 키 일관성)
		List<Long> uniquePlaceIds = placeIds.stream()
				.distinct()
				.sorted()
				.collect(Collectors.toList());
		
		log.info("Processing batch request for {} unique place IDs", uniquePlaceIds.size());
		
		// 캐시 확인
		Cache cache = placeCacheManager.getCache("placeCache");
		if (cache == null) {
			log.warn("Cache not available, fetching all from server");
			return fetchFromServer(uniquePlaceIds);
		}
		
		// 캐시된 항목과 캐시되지 않은 항목 분리
		Map<Long, PlaceInfoResponse> cachedResults = new HashMap<>();
		List<Long> uncachedIds = new ArrayList<>();
		
		for (Long placeId : uniquePlaceIds) {
			PlaceInfoResponse cached = cache.get(placeId, PlaceInfoResponse.class);
			if (cached != null) {
				cachedResults.put(placeId, cached);
				log.debug("Cache hit for placeId: {}", placeId);
			} else {
				uncachedIds.add(placeId);
				log.debug("Cache miss for placeId: {}", placeId);
			}
		}
		
		log.info("Cache status - hits: {}, misses: {}", cachedResults.size(), uncachedIds.size());
		
		// 캐시되지 않은 항목이 없으면 캐시된 결과만 반환
		if (uncachedIds.isEmpty()) {
			return Mono.just(PlaceBatchDetailResponse.builder()
					.results(new ArrayList<>(cachedResults.values()))
					.build());
		}
		
		// 캐시되지 않은 항목만 서버에서 조회
		return fetchFromServer(uncachedIds)
				.map(response -> {
					// 새로 조회한 결과를 캐시에 저장
					if (response.getResults() != null) {
						for (PlaceInfoResponse place : response.getResults()) {
							Long placeId = Long.parseLong(place.getId());
							cache.put(placeId, place);
							cachedResults.put(placeId, place);
							log.debug("Cached new place: placeId={}", placeId);
						}
					}
					
					// 캐시된 결과와 새로 조회한 결과 병합
					List<PlaceInfoResponse> allResults = uniquePlaceIds.stream()
							.map(cachedResults::get)
							.filter(Objects::nonNull)
							.collect(Collectors.toList());
					
					// 실패한 ID 목록 구성 (캐시에도 없고 서버에서도 못 가져온 경우)
					List<Long> failedIds = uniquePlaceIds.stream()
							.filter(id -> !cachedResults.containsKey(id))
							.collect(Collectors.toList());
					
					if (response.getFailed() != null) {
						failedIds.addAll(response.getFailed());
					}
					
					return PlaceBatchDetailResponse.builder()
							.results(allResults)
							.failed(failedIds.isEmpty() ? null : failedIds)
							.build();
				});
	}
	
	/**
	 * 서버에서 Place 정보 배치 조회
	 */
	private Mono<PlaceBatchDetailResponse> fetchFromServer(List<Long> placeIds) {
		log.info("Fetching {} places from server", placeIds.size());
		return placeClient.getPlacesByBatch(placeIds)
				.doOnNext(response -> {
					if (response.getResults() != null) {
						log.info("Fetched {} places from server", response.getResults().size());
					}
					if (response.getFailed() != null && !response.getFailed().isEmpty()) {
						log.warn("Failed to fetch {} places: {}", response.getFailed().size(), response.getFailed());
					}
				});
	}
	
	/**
	 * 캐시 통계 조회
	 */
	public Map<String, Object> getCacheStats() {
		Map<String, Object> stats = new HashMap<>();
		Cache cache = placeCacheManager.getCache("placeCache");
		
		if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
			com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache =
					(com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
			
			stats.put("size", caffeineCache.estimatedSize());
			stats.put("stats", caffeineCache.stats().toString());
		}
		
		return stats;
	}
	
	/**
	 * 캐시 무효화 (특정 Place)
	 */
	public void evictPlace(Long placeId) {
		Cache cache = placeCacheManager.getCache("placeCache");
		if (cache != null) {
			cache.evict(placeId);
			log.info("Evicted place from cache: placeId={}", placeId);
		}
	}
	
	/**
	 * 전체 캐시 클리어
	 */
	public void clearAllCache() {
		Cache cache = placeCacheManager.getCache("placeCache");
		if (cache != null) {
			cache.clear();
			log.info("Cleared all place cache");
		}
	}
}
