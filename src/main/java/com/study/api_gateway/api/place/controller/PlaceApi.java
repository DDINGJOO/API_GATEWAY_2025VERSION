package com.study.api_gateway.api.place.controller;

import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 장소 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Place", description = "장소 조회 API")
public interface PlaceApi {
	
	@Operation(summary = "장소 상세 조회", description = "특정 ID를 가진 장소의 상세 정보를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 공간")
	})
	@GetMapping("/{placeId}")
	Mono<ResponseEntity<BaseResponse>> getPlaceById(
			@Parameter(description = "공간 ID", required = true) @PathVariable String placeId,
			ServerHttpRequest req);
	
	@Operation(summary = "키워드 목록 조회", description = "활성화된 키워드 목록을 조회합니다. 타입별 필터링을 지원합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	@GetMapping("/keywords")
	Mono<ResponseEntity<BaseResponse>> getKeywords(
			@Parameter(description = "키워드 타입 필터 (SPACE_TYPE, INSTRUMENT_EQUIPMENT, AMENITY, OTHER_FEATURE)")
			@RequestParam(required = false) String type,
			ServerHttpRequest req);
	
	@Operation(summary = "장소 통합 검색", description = "다양한 조건으로 장소를 검색합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검색 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/search")
	Mono<ResponseEntity<BaseResponse>> searchPlaces(
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
			@Parameter(description = "등록 상태 필터 (REGISTERED, UNREGISTERED, null)")
			@RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req);
	
	@Operation(summary = "주변 장소 조회", description = "현재 위치 기반으로 주변 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/nearby")
	Mono<ResponseEntity<BaseResponse>> getNearbyPlaces(
			@Parameter(description = "위도", required = true) @RequestParam Double latitude,
			@Parameter(description = "경도", required = true) @RequestParam Double longitude,
			@Parameter(description = "검색 반경(미터)") @RequestParam(defaultValue = "5000") Integer radius,
			@Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
			@Parameter(description = "키워드 ID 목록") @RequestParam(required = false) List<Long> keywordIds,
			@Parameter(description = "주차 가능 여부") @RequestParam(required = false) Boolean parkingAvailable,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
			@Parameter(description = "등록 상태 필터") @RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req);
	
	@Operation(summary = "지역별 장소 조회", description = "특정 지역 내 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/region")
	Mono<ResponseEntity<BaseResponse>> getPlacesByRegion(
			@Parameter(description = "시/도", required = true) @RequestParam String province,
			@Parameter(description = "시/군/구") @RequestParam(required = false) String city,
			@Parameter(description = "동/읍/면") @RequestParam(required = false) String district,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size,
			@Parameter(description = "등록 상태 필터") @RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req);
	
	@Operation(summary = "인기 장소 조회", description = "평점과 리뷰 기준 인기 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/popular")
	Mono<ResponseEntity<BaseResponse>> getPopularPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size,
			@Parameter(description = "등록 상태 필터") @RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req);
	
	@Operation(summary = "최신 장소 조회", description = "최근 등록된 장소를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/recent")
	Mono<ResponseEntity<BaseResponse>> getRecentPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size,
			@Parameter(description = "등록 상태 필터") @RequestParam(required = false) String registrationStatus,
			ServerHttpRequest req);
}
