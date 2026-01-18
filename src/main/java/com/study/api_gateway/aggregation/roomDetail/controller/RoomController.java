package com.study.api_gateway.aggregation.roomDetail.controller;

import com.study.api_gateway.api.place.client.PlaceClient;
import com.study.api_gateway.api.place.dto.response.PlaceBatchDetailResponse;
import com.study.api_gateway.api.place.dto.response.PlaceInfoResponse;
import com.study.api_gateway.api.place.dto.response.PlaceInfoSummary;
import com.study.api_gateway.api.pricing.dto.response.PricingPolicyResponse;
import com.study.api_gateway.api.pricing.dto.response.RoomsPricingBatchResponse;
import com.study.api_gateway.api.product.dto.response.ProductResponse;
import com.study.api_gateway.api.reservation.client.YeYakHaeYoClient;
import com.study.api_gateway.api.room.client.RoomClient;
import com.study.api_gateway.api.room.dto.request.RoomCreateRequest;
import com.study.api_gateway.api.room.dto.response.RoomDetailWithPlaceResponse;
import com.study.api_gateway.api.room.dto.response.RoomDetailWithPricingResponse;
import com.study.api_gateway.api.room.dto.response.RoomSearchWithPlaceResponse;
import com.study.api_gateway.api.room.dto.response.RoomSimpleResponse;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.enrichment.PlaceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 클라이언트 앱용 룸 관리 API
 * RESTful 방식의 룸 CRUD 엔드포인트 제공
 * Room Server, PlaceInfo Server, YeYakHaeYo Server의 데이터를 결합하여 제공 (BFF 패턴)
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/rooms")
@RequiredArgsConstructor
public class RoomController implements RoomApi {
	
	private final RoomClient roomClient;
	private final PlaceClient placeClient;
	private final YeYakHaeYoClient yeYakHaeYoClient;
	private final PlaceCacheService placeCacheService;
	private final ResponseFactory responseFactory;
	
	// ========== Command APIs ==========
	
	/**
	 * 방 생성
	 * POST /bff/v1/rooms
	 */
	@Override
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> createRoom(
			@RequestBody RoomCreateRequest request,
			ServerHttpRequest req
	) {
		log.info("방 생성: roomName={}, placeId={}", request.getRoomName(), request.getPlaceId());
		
		return roomClient.createRoom(request)
				.map(response -> responseFactory.ok(response, req, HttpStatus.CREATED));
	}
	
	/**
	 * 방 삭제
	 * DELETE /bff/v1/rooms/{roomId}
	 */
	@Override
	@DeleteMapping("/{roomId}")
	public Mono<ResponseEntity<BaseResponse>> deleteRoom(
			@PathVariable Long roomId,
			ServerHttpRequest req
	) {
		log.info("방 삭제: roomId={}", roomId);
		
		return roomClient.deleteRoom(roomId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	// ========== Query APIs ==========
	
	/**
	 * 룸 상세 조회 (장소 정보 + 가격 정책 + 이용 가능 상품 포함)
	 * GET /bff/v1/rooms/{roomId}
	 */
	@Override
	@GetMapping("/{roomId}")
	public Mono<ResponseEntity<BaseResponse>> getRoomById(
			@PathVariable Long roomId,
			ServerHttpRequest req
	) {
		log.info("룸 상세 조회: roomId={}", roomId);
		
		return roomClient.getRoomById(roomId)
				.flatMap(roomDetail -> {
					Long placeId = roomDetail.getPlaceId();
					
					// PlaceInfo, PricingPolicy, AvailableProducts를 병렬로 조회 (에러 시 null 또는 빈 리스트 반환)
					Mono<PlaceInfoResponse> placeMono =
							placeClient.getPlaceById(String.valueOf(placeId))
									.onErrorResume(error -> {
										log.warn("장소 정보 조회 실패: placeId={}, error={}", placeId, error.getMessage());
										return Mono.just(null);
									});
					
					Mono<PricingPolicyResponse> pricingMono =
							yeYakHaeYoClient.getPricingPolicy(roomId)
									.onErrorResume(error -> {
										log.warn("가격 정책 조회 실패: roomId={}, error={}", roomId, error.getMessage());
										return Mono.just(null);
									});
					
					Mono<List<ProductResponse>> productsMono =
							yeYakHaeYoClient.getAvailableProductsForRoom(roomId, placeId)
									.onErrorResume(error -> {
										log.warn("이용 가능 상품 조회 실패: roomId={}, placeId={}, error={}", roomId, placeId, error.getMessage());
										return Mono.just(List.of());
									});
					
					// 세 결과를 합쳐서 반환 (일부 실패해도 계속 진행)
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
	 * 룸 검색 (Place 정보 및 가격 정책 포함)
	 * GET /bff/v1/rooms/search
	 */
	@Override
	@GetMapping("/search")
	public Mono<ResponseEntity<BaseResponse>> searchRooms(
			@RequestParam(required = false) String roomName,
			@RequestParam(required = false) List<Long> keywordIds,
			@RequestParam(required = false) Long placeId,
			@RequestParam(required = false) Integer minOccupancy,
			ServerHttpRequest req
	) {
		log.info("룸 검색 시작: roomName={}, keywordIds={}, placeId={}, minOccupancy={}",
				roomName, keywordIds, placeId, minOccupancy);
		
		// 1. Room 검색
		return roomClient.searchRooms(roomName, keywordIds, placeId, minOccupancy)
				.flatMap(rooms -> {
					if (rooms == null || rooms.isEmpty()) {
						log.info("검색 결과 없음");
						return Mono.just(List.<RoomSearchWithPlaceResponse>of());
					}
					
					log.info("Room 검색 결과: {} 개", rooms.size());
					
					// RoomSimpleResponse 리스트로 캐스팅
					List<RoomSimpleResponse> roomList = rooms;
					
					// 2. 고유한 Place ID 추출
					List<Long> uniquePlaceIds = roomList.stream()
							.map(RoomSimpleResponse::getPlaceId)
							.distinct()
							.toList();
					
					// 3. Room ID 리스트 추출
					List<Long> roomIds = roomList.stream()
							.map(RoomSimpleResponse::getRoomId)
							.toList();
					
					log.info("고유 Place ID: {}, Room ID 개수: {}", uniquePlaceIds, roomIds.size());
					
					// 4. Place 정보와 가격 정책을 병렬로 배치 조회
					Mono<PlaceBatchDetailResponse> placesMono =
							placeCacheService.getPlacesByBatchWithCache(uniquePlaceIds)
									.doOnNext(response -> log.info("Place 조회 완료: {} 개",
											response.getResults() != null ? response.getResults().size() : 0));
					
					Mono<RoomsPricingBatchResponse> pricingMono =
							yeYakHaeYoClient.getPricingPoliciesByRoomIds(roomIds)
									.doOnNext(response -> log.info("가격 정책 조회 완료: {} 개",
											response.getRooms() != null ? response.getRooms().size() : 0));
					
					// 5. 모든 데이터를 조합
					return Mono.zip(placesMono, pricingMono)
							.map(tuple -> {
								var places = tuple.getT1();
								var pricingPolicies = tuple.getT2();
								
								// Place 정보를 Map으로 변환 (빠른 조회를 위해)
								Map<Long, PlaceInfoResponse> placeMap = new HashMap<>();
								if (places.getResults() != null) {
									places.getResults().forEach(place ->
											placeMap.put(Long.parseLong(place.getId()), place));
								}
								
								// 가격 정책을 Map으로 변환
								Map<Long, java.math.BigDecimal> priceMap = new HashMap<>();
								if (pricingPolicies.getRooms() != null) {
									pricingPolicies.getRooms().forEach(pricing ->
											priceMap.put(pricing.getRoomId(), pricing.getDefaultPrice()));
								}
								
								// Room 정보와 Place, 가격 정보를 조합
								return roomList.stream()
										.map(room -> {
											// Place 정보 추출
											PlaceInfoResponse placeInfo =
													placeMap.get(room.getPlaceId());
											
											PlaceInfoSummary placeSummary = null;
											if (placeInfo != null) {
												placeSummary = PlaceInfoSummary.builder()
														.category(placeInfo.getCategory())
														.placeName(placeInfo.getPlaceName())
														.placeType(placeInfo.getPlaceType())
														.fullAddress(placeInfo.getLocation() != null &&
																placeInfo.getLocation().getAddress() != null ?
																placeInfo.getLocation().getAddress().getFullAddress() : null)
														.parkingAvailable(placeInfo.getParking() != null ?
																placeInfo.getParking().getAvailable() : false)
														.build();
											}
											
											// 기본 가격 추출
											java.math.BigDecimal defaultPrice = priceMap.get(room.getRoomId());
											
											// RoomSearchWithPlaceResponse 생성
											return RoomSearchWithPlaceResponse.fromRoomSimple(
													room,
													placeSummary,
													defaultPrice,
													3.0  // 리뷰 서버 미구현으로 기본값 3.0
											);
										})
										.collect(java.util.stream.Collectors.toList());
							});
				})
				.map(response -> responseFactory.ok(response, req))
				.onErrorResume(error -> {
					log.error("룸 검색 중 오류 발생: ", error);
					return Mono.just(responseFactory.ok(List.of(), req));
				});
	}
	
	/**
	 * 특정 장소의 룸 목록 조회
	 * GET /bff/v1/rooms/place/{placeId}
	 */
	@Override
	@GetMapping("/place/{placeId}")
	public Mono<ResponseEntity<BaseResponse>> getRoomsByPlaceId(
			@PathVariable Long placeId,
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
	@Override
	@GetMapping("/batch")
	public Mono<ResponseEntity<BaseResponse>> getRoomsByIds(
			@RequestParam List<Long> ids,
			ServerHttpRequest req
	) {
		log.info("여러 룸 일괄 조회: ids={}, count={}", ids, ids.size());
		
		return roomClient.getRoomsByIds(ids)
				.flatMapMany(rooms -> reactor.core.publisher.Flux.fromIterable(rooms)
						.flatMap(room ->
										yeYakHaeYoClient.getPricingPolicy(room.getRoomId())
												.map(pricing -> RoomDetailWithPricingResponse.builder()
														.room(room)
														.pricingPolicy(pricing)
														.build())
												.onErrorResume(error -> {
													log.warn("가격 정책 조회 실패: roomId={}, error={}", room.getRoomId(), error.getMessage());
													// 가격 정책 조회 실패 시 null로 설정하고 계속 진행
													return reactor.core.publisher.Mono.just(
															RoomDetailWithPricingResponse.builder()
																	.room(room)
																	.pricingPolicy(null)
																	.build()
													);
												}),
								// 동시 실행 수 제한 (기본 256, 필요시 조정 가능)
								256
						)
				)
				.collectList()
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 키워드 맵 조회
	 * GET /bff/v1/rooms/keywords
	 */
	@Override
	@GetMapping("/keywords")
	public Mono<ResponseEntity<BaseResponse>> getRoomKeywordMap(ServerHttpRequest req) {
		log.info("키워드 맵 조회");
		
		return roomClient.getRoomKeywordMap()
				.map(response -> responseFactory.ok(response, req));
	}
}
