package com.study.api_gateway.api.room.service;

import com.study.api_gateway.api.room.client.RoomClient;
import com.study.api_gateway.api.room.dto.request.ReservationFieldRequest;
import com.study.api_gateway.api.room.dto.request.RoomCreateRequest;
import com.study.api_gateway.api.room.dto.response.ReservationFieldResponse;
import com.study.api_gateway.api.room.dto.response.RoomDetailResponse;
import com.study.api_gateway.api.room.dto.response.RoomKeywordResponse;
import com.study.api_gateway.api.room.dto.response.RoomSimpleResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Room 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class RoomFacadeService {
	
	private static final String SERVICE_NAME = "room-service";
	private final RoomClient roomClient;
	private final ResilienceOperator resilience;
	
	// ========== Command APIs ==========
	
	public Mono<Long> createRoom(RoomCreateRequest request) {
		return roomClient.createRoom(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Long> deleteRoom(Long roomId) {
		return roomClient.deleteRoom(roomId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ========== Query APIs ==========
	
	public Mono<RoomDetailResponse> getRoomById(Long roomId) {
		return roomClient.getRoomById(roomId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<RoomSimpleResponse>> searchRooms(
			String roomName,
			List<Long> keywordIds,
			Long placeId,
			Integer minOccupancy
	) {
		return roomClient.searchRooms(roomName, keywordIds, placeId, minOccupancy)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<RoomSimpleResponse>> getRoomsByPlaceId(Long placeId) {
		return roomClient.getRoomsByPlaceId(placeId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<RoomDetailResponse>> getRoomsByIds(List<Long> ids) {
		return roomClient.getRoomsByIds(ids)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<Long, RoomKeywordResponse>> getRoomKeywordMap() {
		return roomClient.getRoomKeywordMap()
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ========== Reservation Field APIs ==========
	
	public Mono<List<ReservationFieldResponse>> getReservationFields(Long roomId) {
		return roomClient.getReservationFields(roomId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<ReservationFieldResponse>> replaceReservationFields(
			Long roomId,
			List<ReservationFieldRequest> requests
	) {
		return roomClient.replaceReservationFields(roomId, requests)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
