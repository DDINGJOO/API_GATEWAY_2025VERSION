package com.study.api_gateway.controller.place;

import com.study.api_gateway.client.PlaceClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Place", description = "장소 조회 API")
public class PlaceController {

	private final PlaceClient placeClient;
	private final ResponseFactory responseFactory;

	/**
	 * 장소 통합 검색
	 * GET /bff/v1/places
	 */
	@GetMapping
	@Operation(summary = "장소 통합 검색", description = "다양한 조건으로 장소를 검색합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검색 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
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
			ServerHttpRequest req
	) {
		log.info("장소 검색: keyword={}, location=({}, {}), radius={}",
				keyword, latitude, longitude, radius);

		return placeClient.search(
				keyword, placeName, category, placeType, keywordIds,
				parkingAvailable, latitude, longitude, radius,
				province, city, district, sortBy, sortDirection,
				cursor, size
		).map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 주변 장소 조회 (위치 기반)
	 * GET /bff/v1/places/nearby
	 */
	@GetMapping("/nearby")
	@Operation(summary = "주변 장소 조회", description = "현재 위치 기반으로 주변 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	public Mono<ResponseEntity<BaseResponse>> getNearbyPlaces(
			@Parameter(description = "위도", required = true) @RequestParam Double latitude,
			@Parameter(description = "경도", required = true) @RequestParam Double longitude,
			@Parameter(description = "검색 반경(미터)") @RequestParam(defaultValue = "5000") Integer radius,
			@Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
			@Parameter(description = "키워드 ID 목록") @RequestParam(required = false) List<Long> keywordIds,
			@Parameter(description = "주차 가능 여부") @RequestParam(required = false) Boolean parkingAvailable,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
			ServerHttpRequest req
	) {
		log.info("주변 장소 조회: ({}, {}) 반경 {}m", latitude, longitude, radius);

		return placeClient.search(
				keyword, null, null, null, keywordIds,
				parkingAvailable, latitude, longitude, radius,
				null, null, null, "DISTANCE", "ASC",
				cursor, size
		).map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 지역별 장소 조회
	 * GET /bff/v1/places/region
	 */
	@GetMapping("/region")
	@Operation(summary = "지역별 장소 조회", description = "특정 지역 내 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	public Mono<ResponseEntity<BaseResponse>> getPlacesByRegion(
			@Parameter(description = "시/도", required = true) @RequestParam String province,
			@Parameter(description = "시/군/구") @RequestParam(required = false) String city,
			@Parameter(description = "동/읍/면") @RequestParam(required = false) String district,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
			ServerHttpRequest req
	) {
		log.info("지역별 장소 조회: {}/{}/{}", province, city, district);

		return placeClient.searchByRegion(province, city, district, cursor, size)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 인기 장소 조회
	 * GET /bff/v1/places/popular
	 */
	@GetMapping("/popular")
	@Operation(summary = "인기 장소 조회", description = "평점과 리뷰 기준 인기 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	public Mono<ResponseEntity<BaseResponse>> getPopularPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size,
			ServerHttpRequest req
	) {
		log.info("인기 장소 조회: {} 건", size);

		return placeClient.getPopularPlaces(size)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 최신 장소 조회
	 * GET /bff/v1/places/recent
	 */
	@GetMapping("/recent")
	@Operation(summary = "최신 장소 조회", description = "최근 등록된 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	public Mono<ResponseEntity<BaseResponse>> getRecentPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size,
			ServerHttpRequest req
	) {
		log.info("최신 장소 조회: {} 건", size);

		return placeClient.getRecentPlaces(size)
				.map(response -> responseFactory.ok(response, req));
	}
}