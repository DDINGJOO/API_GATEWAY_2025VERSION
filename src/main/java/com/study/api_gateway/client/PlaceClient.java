package com.study.api_gateway.client;

import com.study.api_gateway.dto.place.response.PlaceInfoResponse;
import com.study.api_gateway.dto.place.response.PlaceSearchResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * PlaceInfo Server와 통신하는 WebClient 기반 클라이언트
 * 조회 전용 API 제공
 */
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
			Integer size
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/search");

		if (keyword != null) builder.queryParam("keyword", keyword);
		if (placeName != null) builder.queryParam("placeName", placeName);
		if (category != null) builder.queryParam("category", category);
		if (placeType != null) builder.queryParam("placeType", placeType);
		if (keywordIds != null && !keywordIds.isEmpty()) {
			keywordIds.forEach(id -> builder.queryParam("keywordIds", id));
		}
		if (parkingAvailable != null) builder.queryParam("parkingAvailable", parkingAvailable);
		if (latitude != null) builder.queryParam("latitude", latitude);
		if (longitude != null) builder.queryParam("longitude", longitude);
		if (radius != null) builder.queryParam("radius", radius);
		if (province != null) builder.queryParam("province", province);
		if (city != null) builder.queryParam("city", city);
		if (district != null) builder.queryParam("district", district);
		if (sortBy != null) builder.queryParam("sortBy", sortBy);
		if (sortDirection != null) builder.queryParam("sortDirection", sortDirection);
		if (cursor != null) builder.queryParam("cursor", cursor);
		if (size != null) builder.queryParam("size", size);

		String uriString = builder.toUriString();

		return webClient.get()
				.uri(uriString)
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
			Integer size
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/search/region");

		builder.queryParam("province", province);
		if (city != null) builder.queryParam("city", city);
		if (district != null) builder.queryParam("district", district);
		if (cursor != null) builder.queryParam("cursor", cursor);
		if (size != null) builder.queryParam("size", size);

		String uriString = builder.toUriString();

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(PlaceSearchResponse.class);
	}

	/**
	 * 인기 장소 조회 API
	 * GET /api/v1/places/search/popular
	 */
	public Mono<PlaceSearchResponse> getPopularPlaces(Integer size) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/search/popular");

		if (size != null) builder.queryParam("size", size);

		String uriString = builder.toUriString();

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(PlaceSearchResponse.class);
	}

	/**
	 * 최신 장소 조회 API
	 * GET /api/v1/places/search/recent
	 */
	public Mono<PlaceSearchResponse> getRecentPlaces(Integer size) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/search/recent");

		if (size != null) builder.queryParam("size", size);

		String uriString = builder.toUriString();

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(PlaceSearchResponse.class);
	}
	
	/**
	 * 장소 상세 조회 API
	 * GET /api/v1/places/{placeId}
	 */
	public Mono<PlaceInfoResponse> getPlaceById(String placeId) {
		String uriString = PREFIX + "/" + placeId;

		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(PlaceInfoResponse.class);
	}
	
	/**
	 * 키워드 목록 조회 API
	 * GET /api/v1/keywords
	 */
	public Mono<List<com.study.api_gateway.dto.place.response.KeywordResponse>> getKeywords(String type) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/v1/keywords");
		
		if (type != null) builder.queryParam("type", type);
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToFlux(com.study.api_gateway.dto.place.response.KeywordResponse.class)
				.collectList();
	}
}
