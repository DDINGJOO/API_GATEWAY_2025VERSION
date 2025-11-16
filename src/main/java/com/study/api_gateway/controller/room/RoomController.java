package com.study.api_gateway.controller.room;

import com.study.api_gateway.client.PlaceClient;
import com.study.api_gateway.client.RoomClient;
import com.study.api_gateway.client.YeYakHaeYoClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.room.response.RoomDetailWithPlaceResponse;
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
							schema = @Schema(implementation = BaseResponse.class)))
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
							schema = @Schema(implementation = BaseResponse.class)))
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
							schema = @Schema(implementation = BaseResponse.class)))
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
							schema = @Schema(implementation = BaseResponse.class)))
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
