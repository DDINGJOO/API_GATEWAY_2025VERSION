package com.study.api_gateway.api.place.controller;

import com.study.api_gateway.api.place.service.PlaceFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 클라이언트 앱용 장소 조회 API
 * RESTful 방식의 조회 전용 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/places")
@RequiredArgsConstructor
public class PlaceController implements PlaceApi {
	
	private final PlaceFacadeService placeFacadeService;
	private final ResponseFactory responseFactory;
	
	@Override
	@GetMapping("/{placeId}")
	public Mono<ResponseEntity<BaseResponse>> getPlaceById(
			@Parameter(description = "공간 ID", required = true) @PathVariable String placeId,
			ServerHttpRequest req
	) {
		log.info("장소 상세 조회: placeId={}", placeId);
		
		return placeFacadeService.getPlaceById(placeId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@GetMapping("/keywords")
	public Mono<ResponseEntity<BaseResponse>> getKeywords(
			@Parameter(description = "키워드 타입 필터 (SPACE_TYPE, INSTRUMENT_EQUIPMENT, AMENITY, OTHER_FEATURE)")
			@RequestParam(required = false) String type,
			ServerHttpRequest req
	) {
		log.info("키워드 목록 조회: type={}", type);
		
		return placeFacadeService.getKeywords(type)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@GetMapping("/search")
	public Mono<ResponseEntity<BaseResponse>> searchPlaces(
			@Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
			@Parameter(description = "장소명") @RequestParam(required = false) String placeName,
			@Parameter(description = "카테고리") @RequestParam(required = false) String category,
			@Parameter(description = "장소 타입") @RequestParam(required = false) String placeType,
			@Parameter(description = "키워드 ID 목록") @RequestParam(required = false) List<Long> keywordIds,
			@Parameter(description = "주차 가능 여부") @RequestParam(required = false) Boolean parkingAvailable,
			@Parameter(description = "위도") @RequestParam(required = false) Double latitude,
			@Parameter(description = "경도") @RequestParam(required = false) Double longitude,
			@Parameter(description = "검색 반경(미터)") @RequestParam(required = false) Integer radius,
			@Parameter(description = "시/도") @RequestParam(required = false) String province,
			@Parameter(description = "시/군/구") @RequestParam(required = false) String city,
			@Parameter(description = "동/읍/면") @RequestParam(required = false) String district,
			@Parameter(description = "정렬 기준 (DISTANCE, RATING, REVIEW_COUNT, CREATED_AT, PLACE_NAME)")
			@RequestParam(required = false) String sortBy,
			@Parameter(description = "정렬 방향 (ASC, DESC)")
			@RequestParam(required = false) String sortDirection,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
			@Parameter(description = "등록 상태 필터 (REGISTERED: 등록 업체만, UNREGISTERED: 미등록 업체만, null: 전체)")
			@RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req
	) {
		log.info("장소 검색: keyword={}, location=({}, {}), radius={}, registrationStatus={}",
				keyword, latitude, longitude, radius, registrationStatus);

		return placeFacadeService.search(
				keyword, placeName, category, placeType, keywordIds,
				parkingAvailable, latitude, longitude, radius,
				province, city, district, sortBy, sortDirection,
				cursor, size, registrationStatus
		).map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@GetMapping("/nearby")
	public Mono<ResponseEntity<BaseResponse>> getNearbyPlaces(
			@Parameter(description = "위도", required = true) @RequestParam Double latitude,
			@Parameter(description = "경도", required = true) @RequestParam Double longitude,
			@Parameter(description = "검색 반경(미터)") @RequestParam(defaultValue = "5000") Integer radius,
			@Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
			@Parameter(description = "키워드 ID 목록") @RequestParam(required = false) List<Long> keywordIds,
			@Parameter(description = "주차 가능 여부") @RequestParam(required = false) Boolean parkingAvailable,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
			@Parameter(description = "등록 상태 필터 (REGISTERED: 등록 업체만, UNREGISTERED: 미등록 업체만, null: 전체)")
			@RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req
	) {
		log.info("주변 장소 조회: ({}, {}) 반경 {}m, registrationStatus={}", latitude, longitude, radius, registrationStatus);

		return placeFacadeService.search(
				keyword, null, null, null, keywordIds,
				parkingAvailable, latitude, longitude, radius,
				null, null, null, "DISTANCE", "ASC",
				cursor, size, registrationStatus
		).map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@GetMapping("/region")
	public Mono<ResponseEntity<BaseResponse>> getPlacesByRegion(
			@Parameter(description = "시/도", required = true) @RequestParam String province,
			@Parameter(description = "시/군/구") @RequestParam(required = false) String city,
			@Parameter(description = "동/읍/면") @RequestParam(required = false) String district,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
			@Parameter(description = "등록 상태 필터 (REGISTERED: 등록 업체만, UNREGISTERED: 미등록 업체만, null: 전체)")
			@RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req
	) {
		log.info("지역별 장소 조회: {}/{}/{}, registrationStatus={}", province, city, district, registrationStatus);
		
		return placeFacadeService.searchByRegion(province, city, district, cursor, size, registrationStatus)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@GetMapping("/popular")
	public Mono<ResponseEntity<BaseResponse>> getPopularPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size,
			@Parameter(description = "등록 상태 필터 (REGISTERED: 등록 업체만, UNREGISTERED: 미등록 업체만, null: 전체)")
			@RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req
	) {
		log.info("인기 장소 조회: {} 건, registrationStatus={}", size, registrationStatus);
		
		return placeFacadeService.getPopularPlaces(size, registrationStatus)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@GetMapping("/recent")
	public Mono<ResponseEntity<BaseResponse>> getRecentPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size,
			@Parameter(description = "등록 상태 필터 (REGISTERED: 등록 업체만, UNREGISTERED: 미등록 업체만, null: 전체)")
			@RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req
	) {
		log.info("최신 장소 조회: {} 건, registrationStatus={}", size, registrationStatus);
		
		return placeFacadeService.getRecentPlaces(size, registrationStatus)
				.map(response -> responseFactory.ok(response, req));
	}
}
