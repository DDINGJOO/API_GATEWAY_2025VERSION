package com.study.api_gateway.api.roomReservation.controller;

import com.study.api_gateway.api.auth.service.AuthFacadeService;
import com.study.api_gateway.api.roomReservation.dto.request.ClosedDatesRequest;
import com.study.api_gateway.api.roomReservation.dto.request.MultiReservationRequest;
import com.study.api_gateway.api.roomReservation.dto.request.RoomSetupRequest;
import com.study.api_gateway.api.roomReservation.service.RoomReservationFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 룸 예약 및 시간 슬롯 관리 API
 * 룸 설정, 휴무일 관리, 예약 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/room-reservations")
@RequiredArgsConstructor
public class RoomReservationController implements RoomReservationApi {
	
	private final RoomReservationFacadeService roomReservationFacadeService;
	private final AuthFacadeService authFacadeService;
	private final ResponseFactory responseFactory;
	
	@Override
	@PostMapping("/setup")
	public Mono<ResponseEntity<BaseResponse>> setupRoom(
			@RequestBody RoomSetupRequest request,
			ServerHttpRequest req
	) {
		log.info("룸 운영 정책 설정 요청: roomId={}", request.roomId());
		
		return roomReservationFacadeService.setupRoom(request)
				.map(response -> responseFactory.ok(response, req, HttpStatus.ACCEPTED));
	}
	
	@Override
	@GetMapping("/setup/{requestId}/status")
	public Mono<ResponseEntity<BaseResponse>> getSlotGenerationStatus(
			@Parameter(description = "슬롯 생성 요청 ID") @PathVariable String requestId,
			ServerHttpRequest req
	) {
		log.info("슬롯 생성 상태 조회: requestId={}", requestId);
		
		return roomReservationFacadeService.getSlotGenerationStatus(requestId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@PostMapping("/setup/closed-dates")
	public Mono<ResponseEntity<BaseResponse>> setClosedDates(
			@RequestBody ClosedDatesRequest request,
			ServerHttpRequest req
	) {
		log.info("휴무일 설정 요청: roomId={}, closedDateCount={}",
				request.roomId(), request.closedDates().size());
		
		return roomReservationFacadeService.setClosedDates(request)
				.map(response -> responseFactory.ok(response, req, HttpStatus.ACCEPTED));
	}
	
	@Override
	@GetMapping("/available-slots")
	public Mono<ResponseEntity<BaseResponse>> getAvailableSlots(
			@Parameter(description = "룸 ID", required = true) @RequestParam Long roomId,
			@Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @RequestParam String date,
			ServerHttpRequest req
	) {
		log.info("예약 가능 슬롯 조회: roomId={}, date={}", roomId, date);
		
		return roomReservationFacadeService.getAvailableSlots(roomId, date)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@PostMapping("/multi")
	public Mono<ResponseEntity<BaseResponse>> reserveMultipleSlots(
			@RequestBody MultiReservationRequest request,
			ServerHttpRequest req
	) {
		// JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
		String userId = req.getHeaders().getFirst("X-User-Id");
		
		log.info("다중 슬롯 예약 요청: userId={}, roomId={}, date={}, slotCount={}",
				userId, request.roomId(), request.slotDate(), request.slotTimes().size());
		
		// 사용자의 전화번호 등록 여부 확인
		return authFacadeService.hasPhoneNumber(userId)
				.flatMap(hasPhoneNumber -> {
					if (!hasPhoneNumber) {
						log.warn("User {} attempted to make reservation without phone number", userId);
						// 전화번호가 없으면 401 에러 반환
						return Mono.just(responseFactory.error(
								"전화번호 등록이 필요합니다",
								HttpStatus.UNAUTHORIZED,
								req
						));
					}
					// 전화번호가 있으면 예약 진행
					return roomReservationFacadeService.reserveMultipleSlots(request)
							.map(response -> responseFactory.ok(response, req));
				})
				.onErrorResume(error -> {
					log.error("Error during reservation process: ", error);
					return Mono.just(responseFactory.error(
							"예약 처리 중 오류가 발생했습니다: " + error.getMessage(),
							HttpStatus.INTERNAL_SERVER_ERROR,
							req
					));
				});
	}
}
