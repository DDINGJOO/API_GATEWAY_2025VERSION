package com.study.api_gateway.controller.placeAndRoom;

import com.study.api_gateway.client.PlaceClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
	 * 장소 상세 조회
	 * GET /bff/v1/places/{placeId}
	 */
	@GetMapping("/{placeId}")
	@Operation(summary = "장소 상세 조회", description = "특정 ID를 가진 장소의 상세 정보를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 공간")
	})
	public Mono<ResponseEntity<BaseResponse>> getPlaceById(
			@Parameter(description = "공간 ID", required = true) @PathVariable String placeId,
			ServerHttpRequest req
	) {
		log.info("장소 상세 조회: placeId={}", placeId);
		
		return placeClient.getPlaceById(placeId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 키워드 목록 조회
	 * GET /bff/v1/places/keywords
	 */
	@GetMapping("/keywords")
	@Operation(summary = "키워드 목록 조회", description = "활성화된 키워드 목록을 조회합니다. 타입별 필터링을 지원합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	public Mono<ResponseEntity<BaseResponse>> getKeywords(
			@Parameter(description = "키워드 타입 필터 (SPACE_TYPE, INSTRUMENT_EQUIPMENT, AMENITY, OTHER_FEATURE)")
			@RequestParam(required = false) String type,
			ServerHttpRequest req
	) {
		log.info("키워드 목록 조회: type={}", type);
		
		return placeClient.getKeywords(type)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 장소 통합 검색
	 * GET /bff/v1/places/search
	 */
	@GetMapping("/search")
	@Operation(summary = "장소 통합 검색", description = "다양한 조건으로 장소를 검색합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검색 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "PlaceSearchSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"items\": [\n      {\n        \"id\": \"1\",\n        \"placeName\": \"밴더 홍대점\",\n        \"description\": \"프리미엄 음악 연습실\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 마포구 양화로 123\",\n        \"latitude\": 37.5556,\n        \"longitude\": 126.9233,\n        \"distance\": 1200.0,\n        \"ratingAverage\": 4.5,\n        \"reviewCount\": 42,\n        \"parkingAvailable\": true,\n        \"parkingType\": \"FREE\",\n        \"thumbnailUrl\": \"https://example.com/place1.jpg\",\n        \"keywords\": [\"연습실\", \"악기대여\"],\n        \"contact\": \"02-1234-5678\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 5,\n        \"roomIds\": [101, 102, 103, 104, 105]\n      }\n    ],\n    \"nextCursor\": \"eyJpZCI6MSwidXBkYXRlZCI6IjIwMjUtMDEtMTAifQ==\",\n    \"hasNext\": true,\n    \"count\": 1,\n    \"totalCount\": 15,\n    \"metadata\": {\n      \"searchTime\": 150,\n      \"sortBy\": \"DISTANCE\",\n      \"sortDirection\": \"ASC\",\n      \"centerLat\": 37.5556,\n      \"centerLng\": 126.9233,\n      \"radiusInMeters\": 5000,\n      \"appliedFilters\": \"keyword=홍대, parkingAvailable=true\"\n    }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/places?keyword=홍대&latitude=37.5556&longitude=126.9233\"\n  }\n}")))
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
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "NearbyPlacesSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"items\": [\n      {\n        \"id\": \"1\",\n        \"placeName\": \"밴더 홍대점\",\n        \"description\": \"프리미엄 음악 연습실\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 마포구 양화로 123\",\n        \"latitude\": 37.5556,\n        \"longitude\": 126.9233,\n        \"distance\": 500.0,\n        \"ratingAverage\": 4.5,\n        \"reviewCount\": 42,\n        \"parkingAvailable\": true,\n        \"parkingType\": \"FREE\",\n        \"thumbnailUrl\": \"https://example.com/place1.jpg\",\n        \"keywords\": [\"연습실\", \"악기대여\"],\n        \"contact\": \"02-1234-5678\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 5,\n        \"roomIds\": [101, 102, 103, 104, 105]\n      },\n      {\n        \"id\": \"2\",\n        \"placeName\": \"리허설 스튜디오\",\n        \"description\": \"24시간 운영 연습실\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 마포구 홍익로 45\",\n        \"latitude\": 37.5560,\n        \"longitude\": 126.9240,\n        \"distance\": 1200.0,\n        \"ratingAverage\": 4.3,\n        \"reviewCount\": 28,\n        \"parkingAvailable\": false,\n        \"parkingType\": null,\n        \"thumbnailUrl\": \"https://example.com/place2.jpg\",\n        \"keywords\": [\"연습실\", \"24시간\"],\n        \"contact\": \"02-5678-9012\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 3,\n        \"roomIds\": [201, 202, 203]\n      }\n    ],\n    \"nextCursor\": null,\n    \"hasNext\": false,\n    \"count\": 2,\n    \"totalCount\": null,\n    \"metadata\": {\n      \"searchTime\": 120,\n      \"sortBy\": \"DISTANCE\",\n      \"sortDirection\": \"ASC\",\n      \"centerLat\": 37.5556,\n      \"centerLng\": 126.9233,\n      \"radiusInMeters\": 5000,\n      \"appliedFilters\": null\n    }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/places/nearby?latitude=37.5556&longitude=126.9233&radius=5000\"\n  }\n}")))
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
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "RegionPlacesSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"items\": [\n      {\n        \"id\": \"1\",\n        \"placeName\": \"밴더 홍대점\",\n        \"description\": \"프리미엄 음악 연습실\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 마포구 서교동 123-45\",\n        \"latitude\": 37.5556,\n        \"longitude\": 126.9233,\n        \"distance\": null,\n        \"ratingAverage\": 4.5,\n        \"reviewCount\": 42,\n        \"parkingAvailable\": true,\n        \"parkingType\": \"FREE\",\n        \"thumbnailUrl\": \"https://example.com/place1.jpg\",\n        \"keywords\": [\"연습실\", \"악기대여\"],\n        \"contact\": \"02-1234-5678\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 5,\n        \"roomIds\": [101, 102, 103, 104, 105]\n      }\n    ],\n    \"nextCursor\": null,\n    \"hasNext\": false,\n    \"count\": 1,\n    \"totalCount\": null,\n    \"metadata\": null\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/places/region?province=서울특별시&city=마포구\"\n  }\n}")))
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
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "PopularPlacesSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"items\": [\n      {\n        \"id\": \"1\",\n        \"placeName\": \"밴더 홍대점\",\n        \"description\": \"프리미엄 음악 연습실\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 마포구 양화로 123\",\n        \"latitude\": 37.5556,\n        \"longitude\": 126.9233,\n        \"distance\": null,\n        \"ratingAverage\": 4.8,\n        \"reviewCount\": 156,\n        \"parkingAvailable\": true,\n        \"parkingType\": \"FREE\",\n        \"thumbnailUrl\": \"https://example.com/place1.jpg\",\n        \"keywords\": [\"연습실\", \"악기대여\"],\n        \"contact\": \"02-1234-5678\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 5,\n        \"roomIds\": [101, 102, 103, 104, 105]\n      },\n      {\n        \"id\": \"2\",\n        \"placeName\": \"뮤직랩 강남점\",\n        \"description\": \"최고급 음악 작업실\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 강남구 테헤란로 456\",\n        \"latitude\": 37.5048,\n        \"longitude\": 127.0495,\n        \"distance\": null,\n        \"ratingAverage\": 4.7,\n        \"reviewCount\": 142,\n        \"parkingAvailable\": true,\n        \"parkingType\": \"PAID\",\n        \"thumbnailUrl\": \"https://example.com/place2.jpg\",\n        \"keywords\": [\"녹음실\", \"믹싱\"],\n        \"contact\": \"02-9876-5432\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 8,\n        \"roomIds\": [301, 302, 303, 304, 305, 306, 307, 308]\n      }\n    ],\n    \"nextCursor\": null,\n    \"hasNext\": false,\n    \"count\": 2,\n    \"totalCount\": null,\n    \"metadata\": null\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/places/popular?size=10\"\n  }\n}")))
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
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "RecentPlacesSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"items\": [\n      {\n        \"id\": \"3\",\n        \"placeName\": \"신규 스튜디오 A\",\n        \"description\": \"신규 오픈 프리미엄 스튜디오\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 종로구 종로 789\",\n        \"latitude\": 37.5700,\n        \"longitude\": 126.9850,\n        \"distance\": null,\n        \"ratingAverage\": null,\n        \"reviewCount\": 0,\n        \"parkingAvailable\": true,\n        \"parkingType\": \"FREE\",\n        \"thumbnailUrl\": \"https://example.com/place3.jpg\",\n        \"keywords\": [\"신규오픈\", \"연습실\"],\n        \"contact\": \"02-1111-2222\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 4,\n        \"roomIds\": [401, 402, 403, 404]\n      },\n      {\n        \"id\": \"2\",\n        \"placeName\": \"뮤직스페이스 B\",\n        \"description\": \"합리적인 가격의 연습실\",\n        \"category\": \"MUSIC_STUDIO\",\n        \"placeType\": \"RENTAL\",\n        \"fullAddress\": \"서울 강서구 강서로 321\",\n        \"latitude\": 37.5485,\n        \"longitude\": 126.8495,\n        \"distance\": null,\n        \"ratingAverage\": 4.2,\n        \"reviewCount\": 15,\n        \"parkingAvailable\": false,\n        \"parkingType\": null,\n        \"thumbnailUrl\": \"https://example.com/place4.jpg\",\n        \"keywords\": [\"연습실\", \"저렴\"],\n        \"contact\": \"02-3333-4444\",\n        \"isActive\": true,\n        \"approvalStatus\": \"APPROVED\",\n        \"roomCount\": 6,\n        \"roomIds\": [501, 502, 503, 504, 505, 506]\n      }\n    ],\n    \"nextCursor\": null,\n    \"hasNext\": false,\n    \"count\": 2,\n    \"totalCount\": null,\n    \"metadata\": null\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/places/recent?size=10\"\n  }\n}")))
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
