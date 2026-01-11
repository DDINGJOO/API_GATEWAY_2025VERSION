package com.study.api_gateway.api.place.service;

import com.study.api_gateway.api.place.client.PlaceClient;
import com.study.api_gateway.api.place.dto.response.KeywordResponse;
import com.study.api_gateway.api.place.dto.response.PlaceBatchDetailResponse;
import com.study.api_gateway.api.place.dto.response.PlaceInfoResponse;
import com.study.api_gateway.api.place.dto.response.PlaceSearchResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Place 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class PlaceFacadeService {

	private final PlaceClient placeClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "place-service";

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
		return placeClient.search(keyword, placeName, category, placeType, keywordIds, parkingAvailable,
						latitude, longitude, radius, province, city, district, sortBy, sortDirection, cursor, size, registrationStatus)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<PlaceSearchResponse> searchByRegion(
			String province,
			String city,
			String district,
			String cursor,
			Integer size,
			String registrationStatus
	) {
		return placeClient.searchByRegion(province, city, district, cursor, size, registrationStatus)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<PlaceSearchResponse> getPopularPlaces(Integer size, String registrationStatus) {
		return placeClient.getPopularPlaces(size, registrationStatus)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<PlaceSearchResponse> getRecentPlaces(Integer size, String registrationStatus) {
		return placeClient.getRecentPlaces(size, registrationStatus)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<PlaceInfoResponse> getPlaceById(String placeId) {
		return placeClient.getPlaceById(placeId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<List<KeywordResponse>> getKeywords(String type) {
		return placeClient.getKeywords(type)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<PlaceBatchDetailResponse> getPlacesByBatch(List<Long> placeIds) {
		return placeClient.getPlacesByBatch(placeIds)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
