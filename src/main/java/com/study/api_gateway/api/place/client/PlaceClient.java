package com.study.api_gateway.api.place.client;

import com.study.api_gateway.api.place.dto.request.PlaceBatchDetailRequest;
import com.study.api_gateway.api.place.dto.response.PlaceBatchDetailResponse;
import com.study.api_gateway.api.place.dto.response.PlaceInfoResponse;
import com.study.api_gateway.api.place.dto.response.PlaceSearchResponse;
import com.study.api_gateway.api.place.dto.response.KeywordResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * PlaceInfo Server와 통신하는 WebClient 기반 클라이언트
 * 조회 전용 API 제공
 */
@Slf4j
@Component
public class PlaceClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/v1/places";
	
	public PlaceClient(@Qualifier("placeInfoWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 통합 검색 API
	 * GET /api/v1/places/search
	 */
	public Mono<PlaceSearchResponse> search(
			String keyword,
			String placeName,
			String category,
			String placeType,
			List<Long> keywordIds,
			Boolean parkingAvailable,
			Double latitude,
			Double longitude,
			Integer radius,
			String province,
			String city,
			String district,
			String sortBy,
			String sortDirection,
			String cursor,
			Integer size,
			String registrationStatus
	) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/search");

					if (keyword != null) uriBuilder.queryParam("keyword", keyword);
					if (placeName != null) uriBuilder.queryParam("placeName", placeName);
					if (category != null) uriBuilder.queryParam("category", category);
					if (placeType != null) uriBuilder.queryParam("placeType", placeType);
					if (keywordIds != null && !keywordIds.isEmpty()) {
						keywordIds.forEach(id -> uriBuilder.queryParam("keywordIds", id));
					}
					if (parkingAvailable != null) uriBuilder.queryParam("parkingAvailable", parkingAvailable);
					if (latitude != null) uriBuilder.queryParam("latitude", latitude);
					if (longitude != null) uriBuilder.queryParam("longitude", longitude);
					if (radius != null) uriBuilder.queryParam("radius", radius);
					if (province != null) uriBuilder.queryParam("province", province);
					if (city != null) uriBuilder.queryParam("city", city);
					if (district != null) uriBuilder.queryParam("district", district);
					if (sortBy != null) uriBuilder.queryParam("sortBy", sortBy);
					if (sortDirection != null) uriBuilder.queryParam("sortDirection", sortDirection);
					if (cursor != null) uriBuilder.queryParam("cursor", cursor);
					if (size != null) uriBuilder.queryParam("size", size);
					if (registrationStatus != null) uriBuilder.queryParam("registrationStatus", registrationStatus);

					return uriBuilder.build();
				})
				.retrieve()
				.bodyToMono(PlaceSearchResponse.class);
	}
	
	/**
	 * 지역별 검색 API
	 * GET /api/v1/places/search/region
	 */
	public Mono<PlaceSearchResponse> searchByRegion(
			String province,
			String city,
			String district,
			String cursor,
			Integer size,
			String registrationStatus
	) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/search/region");

					uriBuilder.queryParam("province", province);
					if (city != null) uriBuilder.queryParam("city", city);
					if (district != null) uriBuilder.queryParam("district", district);
					if (cursor != null) uriBuilder.queryParam("cursor", cursor);
					if (size != null) uriBuilder.queryParam("size", size);
					if (registrationStatus != null) uriBuilder.queryParam("registrationStatus", registrationStatus);

					return uriBuilder.build();
				})
				.retrieve()
				.bodyToMono(PlaceSearchResponse.class);
	}
	
	/**
	 * 인기 장소 조회 API
	 * GET /api/v1/places/search/popular
	 */
	public Mono<PlaceSearchResponse> getPopularPlaces(Integer size, String registrationStatus) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/search/popular");

					if (size != null) uriBuilder.queryParam("size", size);
					if (registrationStatus != null) uriBuilder.queryParam("registrationStatus", registrationStatus);

					return uriBuilder.build();
				})
				.retrieve()
				.bodyToMono(PlaceSearchResponse.class);
	}
	
	/**
	 * 최신 장소 조회 API
	 * GET /api/v1/places/search/recent
	 */
	public Mono<PlaceSearchResponse> getRecentPlaces(Integer size, String registrationStatus) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/search/recent");
					
					if (size != null) uriBuilder.queryParam("size", size);
					if (registrationStatus != null) uriBuilder.queryParam("registrationStatus", registrationStatus);

					return uriBuilder.build();
				})
				.retrieve()
				.bodyToMono(PlaceSearchResponse.class);
	}
	
	/**
	 * 장소 상세 조회 API
	 * GET /api/v1/places/{placeId}
	 */
	public Mono<PlaceInfoResponse> getPlaceById(String placeId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/{placeId}")
						.build(placeId))
				.retrieve()
				.bodyToMono(PlaceInfoResponse.class);
	}
	
	/**
	 * 키워드 목록 조회 API
	 * GET /api/v1/keywords
	 */
	public Mono<List<KeywordResponse>> getKeywords(String type) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path("/api/v1/keywords");
					
					if (type != null) uriBuilder.queryParam("type", type);
					
					return uriBuilder.build();
				})
				.retrieve()
				.bodyToFlux(KeywordResponse.class)
				.collectList();
	}
	
	/**
	 * 여러 장소 배치 상세 조회 API
	 * POST /api/v1/places/search/batch/details
	 *
	 * @param placeIds 조회할 장소 ID 목록 (최대 50개)
	 * @return 조회 성공한 장소 정보 목록과 실패한 ID 목록
	 */
	public Mono<PlaceBatchDetailResponse> getPlacesByBatch(List<Long> placeIds) {
		if (placeIds == null || placeIds.isEmpty()) {
			return Mono.just(PlaceBatchDetailResponse.builder()
					.results(List.of())
					.build());
		}
		
		PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
				.placeIds(placeIds)
				.build();
		
		return webClient.post()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/search/batch/details")
						.build())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(PlaceBatchDetailResponse.class)
				.onErrorResume(error -> {
					log.error("Place 배치 조회 실패: placeIds={}, error={}", placeIds, error.getMessage());
					// 에러 발생 시 빈 결과 반환
					return Mono.just(PlaceBatchDetailResponse.builder()
							.results(List.of())
							.failed(placeIds)
							.build());
				});
	}
}
