package com.study.api_gateway.api.roomReservation.service;

import com.study.api_gateway.api.roomReservation.client.RoomReservationClient;
import com.study.api_gateway.api.roomReservation.dto.request.ClosedDatesRequest;
import com.study.api_gateway.api.roomReservation.dto.request.MultiReservationRequest;
import com.study.api_gateway.api.roomReservation.dto.request.RoomSetupRequest;
import com.study.api_gateway.api.roomReservation.dto.response.*;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RoomReservation 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class RoomReservationFacadeService {

	private final RoomReservationClient roomReservationClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "room-reservation-service";

	public Mono<RoomSetupResponse> setupRoom(RoomSetupRequest request) {
		return roomReservationClient.setupRoom(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<SlotGenerationStatusResponse> getSlotGenerationStatus(String requestId) {
		return roomReservationClient.getSlotGenerationStatus(requestId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ClosedDatesResponse> setClosedDates(ClosedDatesRequest request) {
		return roomReservationClient.setClosedDates(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<List<AvailableSlotResponse>> getAvailableSlots(Long roomId, String date) {
		return roomReservationClient.getAvailableSlots(roomId, date)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<MultiReservationResponse> reserveMultipleSlots(MultiReservationRequest request) {
		return roomReservationClient.reserveMultipleSlots(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
