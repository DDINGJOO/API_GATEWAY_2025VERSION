package com.study.api_gateway.client;

import com.study.api_gateway.dto.roomReservation.request.ClosedDatesRequest;
import com.study.api_gateway.dto.roomReservation.request.MultiReservationRequest;
import com.study.api_gateway.dto.roomReservation.request.RoomSetupRequest;
import com.study.api_gateway.dto.roomReservation.response.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Room Reservation Server와 통신하는 WebClient 기반 클라이언트
 * 룸 설정 및 예약 API 제공
 */
@Component
public class RoomReservationClient {
	private final WebClient webClient;
	
	public RoomReservationClient(@Qualifier("roomReservationWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 룸 운영 정책 설정 및 슬롯 생성 요청
	 * POST /api/rooms/setup
	 */
	public Mono<RoomSetupResponse> setupRoom(RoomSetupRequest request) {
		return webClient.post()
				.uri("/api/rooms/setup")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(RoomSetupResponse.class);
	}
	
	/**
	 * 슬롯 생성 상태 조회
	 * GET /api/rooms/setup/{requestId}/status
	 */
	public Mono<SlotGenerationStatusResponse> getSlotGenerationStatus(String requestId) {
		String uriString = "/api/rooms/setup/" + requestId + "/status";
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(SlotGenerationStatusResponse.class);
	}
	
	/**
	 * 휴무일 설정
	 * POST /api/rooms/setup/closed-dates
	 */
	public Mono<ClosedDatesResponse> setClosedDates(ClosedDatesRequest request) {
		return webClient.post()
				.uri("/api/rooms/setup/closed-dates")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ClosedDatesResponse.class);
	}
	
	/**
	 * 예약 가능 슬롯 조회
	 * GET /api/v1/reservations/available-slots
	 */
	public Mono<List<AvailableSlotResponse>> getAvailableSlots(Long roomId, String date) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/v1/reservations/available-slots");
		
		builder.queryParam("roomId", roomId);
		builder.queryParam("date", date);
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToFlux(AvailableSlotResponse.class)
				.collectList();
	}
	
	/**
	 * 다중 슬롯 예약
	 * POST /api/v1/reservations/multi
	 */
	public Mono<MultiReservationResponse> reserveMultipleSlots(MultiReservationRequest request) {
		return webClient.post()
				.uri("/api/v1/reservations/multi")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(MultiReservationResponse.class);
	}
}
