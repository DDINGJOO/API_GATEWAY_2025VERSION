package com.study.api_gateway.api.room.client;

import com.study.api_gateway.api.room.dto.request.ReservationFieldRequest;
import com.study.api_gateway.api.room.dto.request.RoomCreateRequest;
import com.study.api_gateway.api.room.dto.response.ReservationFieldResponse;
import com.study.api_gateway.api.room.dto.response.RoomDetailResponse;
import com.study.api_gateway.api.room.dto.response.RoomSimpleResponse;
import com.study.api_gateway.api.room.dto.response.RoomKeywordResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Room Server와 통신하는 WebClient 기반 클라이언트
 * 룸 생성, 조회, 삭제 API 제공
 */
@Component
public class RoomClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/rooms";
	
	public RoomClient(@Qualifier("roomWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	// ========== Command APIs ==========
	
	/**
	 * 방 생성
	 * POST /api/rooms
	 */
	public Mono<Long> createRoom(RoomCreateRequest request) {
		return webClient.post()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX)
						.build())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(Long.class);
	}
	
	/**
	 * 방 삭제
	 * DELETE /api/rooms/{roomId}
	 */
	public Mono<Long> deleteRoom(Long roomId) {
		return webClient.delete()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/{roomId}")
						.build(roomId))
				.retrieve()
				.bodyToMono(Long.class);
	}
	
	// ========== Query APIs ==========
	
	/**
	 * 룸 상세 조회 API
	 * GET /api/rooms/{roomId}
	 */
	public Mono<RoomDetailResponse> getRoomById(Long roomId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/{roomId}")
						.build(roomId))
				.retrieve()
				.bodyToMono(RoomDetailResponse.class);
	}
	
	/**
	 * 룸 검색 API
	 * GET /api/rooms/search
	 */
	public Mono<List<RoomSimpleResponse>> searchRooms(
			String roomName,
			List<Long> keywordIds,
			Long placeId,
			Integer minOccupancy
	) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/search");
					
					if (roomName != null) uriBuilder.queryParam("roomName", roomName);
					if (keywordIds != null && !keywordIds.isEmpty()) {
						keywordIds.forEach(id -> uriBuilder.queryParam("keywordIds", id));
					}
					if (placeId != null) uriBuilder.queryParam("placeId", placeId);
					if (minOccupancy != null) uriBuilder.queryParam("minOccupancy", minOccupancy);
					
					return uriBuilder.build();
				})
				.retrieve()
				.bodyToFlux(RoomSimpleResponse.class)
				.collectList();
	}
	
	/**
	 * 특정 장소의 룸 목록 조회 API
	 * GET /api/rooms/place/{placeId}
	 */
	public Mono<List<RoomSimpleResponse>> getRoomsByPlaceId(Long placeId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/place/{placeId}")
						.build(placeId))
				.retrieve()
				.bodyToFlux(RoomSimpleResponse.class)
				.collectList();
	}
	
	/**
	 * 여러 룸 일괄 조회 API
	 * GET /api/rooms/batch?ids=1,2,3
	 */
	public Mono<List<RoomDetailResponse>> getRoomsByIds(List<Long> ids) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/batch");
					
					if (ids != null && !ids.isEmpty()) {
						ids.forEach(id -> uriBuilder.queryParam("ids", id));
					}
					
					return uriBuilder.build();
				})
				.retrieve()
				.bodyToFlux(RoomDetailResponse.class)
				.collectList();
	}
	
	/**
	 * 룸 키워드 맵 조회 API
	 * GET /api/rooms/keywords
	 */
	public Mono<Map<Long, RoomKeywordResponse>> getRoomKeywordMap() {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/keywords")
						.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<Long, RoomKeywordResponse>>() {
				});
	}

	// ========== Reservation Field APIs ==========

	/**
	 * 예약 필드 목록 조회
	 * GET /api/rooms/{roomId}/reservation-fields
	 */
	public Mono<List<ReservationFieldResponse>> getReservationFields(Long roomId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/{roomId}/reservation-fields")
						.build(roomId))
				.retrieve()
				.bodyToFlux(ReservationFieldResponse.class)
				.collectList();
	}

	/**
	 * 예약 필드 전체 교체
	 * PUT /api/rooms/{roomId}/reservation-fields
	 */
	public Mono<List<ReservationFieldResponse>> replaceReservationFields(
			Long roomId,
			List<ReservationFieldRequest> requests
	) {
		return webClient.put()
				.uri(uriBuilder -> uriBuilder
						.path(PREFIX + "/{roomId}/reservation-fields")
						.build(roomId))
				.bodyValue(requests)
				.retrieve()
				.bodyToFlux(ReservationFieldResponse.class)
				.collectList();
	}
}
