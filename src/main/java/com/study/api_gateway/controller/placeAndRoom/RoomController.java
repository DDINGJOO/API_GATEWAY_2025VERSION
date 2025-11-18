package com.study.api_gateway.controller.placeAndRoom;

import com.study.api_gateway.client.PlaceClient;
import com.study.api_gateway.client.RoomClient;
import com.study.api_gateway.client.YeYakHaeYoClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.room.response.RoomDetailWithPlaceResponse;
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
 * 클라이언트 앱용 룸 조회 API
 * RESTful 방식의 조회 전용 엔드포인트 제공
 * Room Server, PlaceInfo Server, YeYakHaeYo Server의 데이터를 결합하여 제공 (BFF 패턴)
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room", description = "룸 조회 API")
public class RoomController {
	
	private final RoomClient roomClient;
	private final PlaceClient placeClient;
	private final YeYakHaeYoClient yeYakHaeYoClient;
	private final ResponseFactory responseFactory;
	
	/**
	 * 룸 상세 조회 (장소 정보 + 가격 정책 + 이용 가능 상품 포함)
	 * GET /bff/v1/rooms/{roomId}
	 */
	@GetMapping("/{roomId}")
	@Operation(summary = "룸 상세 조회", description = "룸 상세 정보, 장소 정보, 가격 정책, 이용 가능한 상품을 함께 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "RoomDetailSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"room\": {\n      \"roomId\": 101,\n      \"roomName\": \"A룸\",\n      \"placeId\": 1,\n      \"status\": \"OPEN\",\n      \"timeSlot\": \"HOUR\",\n      \"furtherDetails\": [\"방음 시설 완비\", \"24시간 이용 가능\"],\n      \"cautionDetails\": [\"흡연 금지\", \"음식물 반입 금지\"],\n      \"imageUrls\": [\"https://example.com/room1.jpg\"],\n      \"keywordIds\": [1, 2, 3]\n    },\n    \"place\": {\n      \"id\": \"1\",\n      \"userId\": \"user123\",\n      \"placeName\": \"밴더 홍대점\",\n      \"description\": \"프리미엄 음악 연습실\",\n      \"category\": \"MUSIC_STUDIO\",\n      \"placeType\": \"RENTAL\",\n      \"contact\": {\n        \"contact\": \"02-1234-5678\",\n        \"email\": \"contact@example.com\",\n        \"websites\": [\"https://bander.com\"],\n        \"socialLinks\": [\"https://instagram.com/bander\"]\n      },\n      \"location\": {\n        \"address\": {\n          \"province\": \"서울특별시\",\n          \"city\": \"마포구\",\n          \"district\": \"서교동\",\n          \"fullAddress\": \"서울 마포구 서교동 123-45\",\n          \"addressDetail\": \"3층\",\n          \"postalCode\": \"04001\",\n          \"shortAddress\": \"서울 마포구\"\n        },\n        \"latitude\": 37.5556,\n        \"longitude\": 126.9233,\n        \"locationGuide\": \"홍대입구역 9번 출구에서 도보 5분\"\n      },\n      \"parking\": {\n        \"available\": true,\n        \"parkingType\": \"FREE\",\n        \"description\": \"건물 지하 주차장 이용 가능\"\n      },\n      \"imageUrls\": [\"https://example.com/place1.jpg\"],\n      \"keywords\": [{\n        \"id\": 1,\n        \"name\": \"연습실\",\n        \"type\": \"SPACE_TYPE\",\n        \"description\": \"음악 연습 공간\",\n        \"displayOrder\": 1\n      }],\n      \"isActive\": true,\n      \"approvalStatus\": \"APPROVED\",\n      \"ratingAverage\": 4.5,\n      \"reviewCount\": 42,\n      \"roomCount\": 5,\n      \"roomIds\": [101, 102, 103],\n      \"createdAt\": \"2025-01-01T10:00:00\",\n      \"updatedAt\": \"2025-01-10T15:30:00\"\n    },\n    \"pricingPolicy\": {\n      \"roomId\": 101,\n      \"placeId\": 1,\n      \"timeSlot\": \"1시간\",\n      \"defaultPrice\": 15000,\n      \"timeRangePrices\": [\n        {\n          \"dayOfWeek\": \"MONDAY\",\n          \"startTime\": \"09:00\",\n          \"endTime\": \"18:00\",\n          \"price\": 15000\n        },\n        {\n          \"dayOfWeek\": \"SATURDAY\",\n          \"startTime\": \"09:00\",\n          \"endTime\": \"22:00\",\n          \"price\": 20000\n        }\n      ]\n    },\n    \"availableProducts\": [\n      {\n        \"productId\": 1,\n        \"scope\": \"ROOM\",\n        \"placeId\": 1,\n        \"roomId\": 101,\n        \"name\": \"1시간 이용권\",\n        \"pricingStrategy\": {\n          \"pricingType\": \"FIXED\",\n          \"initialPrice\": 15000,\n          \"additionalPrice\": 0\n        },\n        \"totalQuantity\": 10\n      }\n    ]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/rooms/101\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getRoomById(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			ServerHttpRequest req
	) {
		log.info("룸 상세 조회: roomId={}", roomId);
		
		return roomClient.getRoomById(roomId)
				.flatMap(roomDetail -> {
					Long placeId = roomDetail.getPlaceId();
					
					// PlaceInfo, PricingPolicy, AvailableProducts를 병렬로 조회
					Mono<com.study.api_gateway.dto.place.response.PlaceInfoResponse> placeMono =
							placeClient.getPlaceById(String.valueOf(placeId));
					Mono<com.study.api_gateway.dto.pricing.response.PricingPolicyResponse> pricingMono =
							yeYakHaeYoClient.getPricingPolicy(roomId);
					Mono<List<com.study.api_gateway.dto.product.response.ProductResponse>> productsMono =
							yeYakHaeYoClient.getAvailableProductsForRoom(roomId, placeId);
					
					// 세 결과를 합쳐서 반환
					return Mono.zip(placeMono, pricingMono, productsMono)
							.map(tuple -> RoomDetailWithPlaceResponse.builder()
									.room(roomDetail)
									.place(tuple.getT1())
									.pricingPolicy(tuple.getT2())
									.availableProducts(tuple.getT3())
									.build());
				})
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 룸 검색
	 * GET /bff/v1/rooms/search
	 */
	@GetMapping("/search")
	@Operation(summary = "룸 검색", description = "다양한 조건으로 룸을 검색합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "검색 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "RoomSearchSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"roomId\": 101,\n      \"roomName\": \"A룸\",\n      \"placeId\": 1,\n      \"timeSlot\": \"HOUR\",\n      \"imageUrls\": [\"https://example.com/room1.jpg\"],\n      \"keywordIds\": [1, 2, 3]\n    },\n    {\n      \"roomId\": 102,\n      \"roomName\": \"B룸\",\n      \"placeId\": 1,\n      \"timeSlot\": \"HALFHOUR\",\n      \"imageUrls\": [\"https://example.com/room2.jpg\"],\n      \"keywordIds\": [1, 4, 5]\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/rooms/search?placeId=1\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> searchRooms(
			@Parameter(description = "룸 이름") @RequestParam(required = false) String roomName,
			@Parameter(description = "키워드 ID 목록") @RequestParam(required = false) List<Long> keywordIds,
			@Parameter(description = "장소 ID") @RequestParam(required = false) Long placeId,
			ServerHttpRequest req
	) {
		log.info("룸 검색: roomName={}, keywordIds={}, placeId={}", roomName, keywordIds, placeId);
		
		return roomClient.searchRooms(roomName, keywordIds, placeId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 특정 장소의 룸 목록 조회
	 * GET /bff/v1/rooms/place/{placeId}
	 */
	@GetMapping("/place/{placeId}")
	@Operation(summary = "장소별 룸 목록 조회", description = "특정 장소에 속한 모든 룸을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "PlaceRoomsSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"roomId\": 101,\n      \"roomName\": \"A룸\",\n      \"placeId\": 1,\n      \"timeSlot\": \"HOUR\",\n      \"imageUrls\": [\"https://example.com/room1.jpg\"],\n      \"keywordIds\": [1, 2, 3]\n    },\n    {\n      \"roomId\": 102,\n      \"roomName\": \"B룸\",\n      \"placeId\": 1,\n      \"timeSlot\": \"HALFHOUR\",\n      \"imageUrls\": [\"https://example.com/room2.jpg\"],\n      \"keywordIds\": [1, 4]\n    },\n    {\n      \"roomId\": 103,\n      \"roomName\": \"C룸\",\n      \"placeId\": 1,\n      \"timeSlot\": \"HOUR\",\n      \"imageUrls\": [\"https://example.com/room3.jpg\"],\n      \"keywordIds\": [2, 5]\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/rooms/place/1\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getRoomsByPlaceId(
			@Parameter(description = "장소 ID", required = true) @PathVariable Long placeId,
			ServerHttpRequest req
	) {
		log.info("장소별 룸 목록 조회: placeId={}", placeId);
		
		return roomClient.getRoomsByPlaceId(placeId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 여러 룸 일괄 조회 (가격 정책 포함)
	 * GET /bff/v1/rooms/batch?ids=1,2,3
	 */
	@GetMapping("/batch")
	@Operation(summary = "여러 룸 일괄 조회", description = "여러 룸의 상세 정보와 가격 정책을 한 번에 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "BatchRoomsSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"room\": {\n        \"roomId\": 101,\n        \"roomName\": \"A룸\",\n        \"placeId\": 1,\n        \"status\": \"OPEN\",\n        \"timeSlot\": \"HOUR\",\n        \"furtherDetails\": [\"방음 시설 완비\", \"24시간 이용 가능\"],\n        \"cautionDetails\": [\"흡연 금지\", \"음식물 반입 금지\"],\n        \"imageUrls\": [\"https://example.com/room101_1.jpg\", \"https://example.com/room101_2.jpg\"],\n        \"keywordIds\": [1, 2, 3]\n      },\n      \"pricingPolicy\": {\n        \"roomId\": 101,\n        \"placeId\": 1,\n        \"timeSlot\": \"1시간\",\n        \"defaultPrice\": 15000,\n        \"timeRangePrices\": [\n          {\n            \"dayOfWeek\": \"MONDAY\",\n            \"startTime\": \"09:00\",\n            \"endTime\": \"18:00\",\n            \"price\": 15000\n          },\n          {\n            \"dayOfWeek\": \"SATURDAY\",\n            \"startTime\": \"09:00\",\n            \"endTime\": \"22:00\",\n            \"price\": 18000\n          }\n        ]\n      }\n    },\n    {\n      \"room\": {\n        \"roomId\": 102,\n        \"roomName\": \"B룸\",\n        \"placeId\": 1,\n        \"status\": \"OPEN\",\n        \"timeSlot\": \"HOUR\",\n        \"furtherDetails\": [\"대형 룸\", \"최대 10인 수용\"],\n        \"cautionDetails\": [\"흡연 금지\"],\n        \"imageUrls\": [\"https://example.com/room102_1.jpg\"],\n        \"keywordIds\": [1, 4, 5]\n      },\n      \"pricingPolicy\": {\n        \"roomId\": 102,\n        \"placeId\": 1,\n        \"timeSlot\": \"1시간\",\n        \"defaultPrice\": 20000,\n        \"timeRangePrices\": [\n          {\n            \"dayOfWeek\": \"FRIDAY\",\n            \"startTime\": \"09:00\",\n            \"endTime\": \"18:00\",\n            \"price\": 20000\n          },\n          {\n            \"dayOfWeek\": \"SUNDAY\",\n            \"startTime\": \"09:00\",\n            \"endTime\": \"22:00\",\n            \"price\": 24000\n          }\n        ]\n      }\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/rooms/batch?ids=101,102\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getRoomsByIds(
			@Parameter(description = "룸 ID 목록", required = true) @RequestParam List<Long> ids,
			ServerHttpRequest req
	) {
		log.info("여러 룸 일괄 조회: ids={}", ids);
		
		return roomClient.getRoomsByIds(ids)
				.flatMap(rooms -> {
					// 각 룸의 가격 정책을 병렬로 조회
					List<Mono<com.study.api_gateway.dto.room.response.RoomDetailWithPricingResponse>> enrichedRooms = rooms.stream()
							.map(room -> yeYakHaeYoClient.getPricingPolicy(room.getRoomId())
									.map(pricing -> com.study.api_gateway.dto.room.response.RoomDetailWithPricingResponse.builder()
											.room(room)
											.pricingPolicy(pricing)
											.build()))
							.toList();
					
					// 모든 조회 완료 후 리스트로 변환
					return Mono.zip(enrichedRooms, arrays ->
							java.util.Arrays.stream(arrays)
									.map(obj -> (com.study.api_gateway.dto.room.response.RoomDetailWithPricingResponse) obj)
									.toList()
					);
				})
				.map(response -> responseFactory.ok(response, req));
	}
}
