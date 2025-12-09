package com.study.api_gateway.controller.roomReservation;

import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.client.RoomReservationClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.roomReservation.request.ClosedDatesRequest;
import com.study.api_gateway.dto.roomReservation.request.MultiReservationRequest;
import com.study.api_gateway.dto.roomReservation.request.RoomSetupRequest;
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
@Tag(name = "Room Reservation", description = "룸 예약 및 시간 슬롯 관리 API")
public class RoomReservationController {
	
	private final RoomReservationClient roomReservationClient;
	private final AuthClient authClient;
	private final ResponseFactory responseFactory;
	
	/**
	 * 룸 운영 정책 설정 및 슬롯 생성 요청
	 * POST /bff/v1/room-reservations/setup
	 */
	@PostMapping("/setup")
	@Operation(summary = "룸 운영 정책 설정", description = "룸의 운영 시간 정책을 설정하고 시간 슬롯을 자동으로 생성합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "202", description = "요청 접수됨",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "409", description = "운영 정책이 이미 존재")
	})
	public Mono<ResponseEntity<BaseResponse>> setupRoom(
			@RequestBody RoomSetupRequest request,
			ServerHttpRequest req
	) {
		log.info("룸 운영 정책 설정 요청: roomId={}", request.roomId());
		
		return roomReservationClient.setupRoom(request)
				.map(response -> responseFactory.ok(response, req, HttpStatus.ACCEPTED));
	}
	
	/**
	 * 슬롯 생성 상태 조회
	 * GET /bff/v1/room-reservations/setup/{requestId}/status
	 */
	@GetMapping("/setup/{requestId}/status")
	@Operation(summary = "슬롯 생성 상태 조회", description = "슬롯 생성 요청의 진행 상태를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> getSlotGenerationStatus(
			@Parameter(description = "슬롯 생성 요청 ID") @PathVariable String requestId,
			ServerHttpRequest req
	) {
		log.info("슬롯 생성 상태 조회: requestId={}", requestId);
		
		return roomReservationClient.getSlotGenerationStatus(requestId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 휴무일 설정
	 * POST /bff/v1/room-reservations/setup/closed-dates
	 */
	@PostMapping("/setup/closed-dates")
	@Operation(summary = "휴무일 설정", description = "룸의 휴무일을 설정하고 해당 날짜의 슬롯 상태를 CLOSED로 변경합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "202", description = "요청 접수됨",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "404", description = "룸을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> setClosedDates(
			@RequestBody ClosedDatesRequest request,
			ServerHttpRequest req
	) {
		log.info("휴무일 설정 요청: roomId={}, closedDateCount={}",
				request.roomId(), request.closedDates().size());
		
		return roomReservationClient.setClosedDates(request)
				.map(response -> responseFactory.ok(response, req, HttpStatus.ACCEPTED));
	}
	
	/**
	 * 예약 가능 슬롯 조회
	 * GET /bff/v1/room-reservations/available-slots
	 */
	@GetMapping("/available-slots")
	@Operation(summary = "예약 가능 슬롯 조회", description = "특정 룸의 특정 날짜에 예약 가능한 슬롯 목록을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	public Mono<ResponseEntity<BaseResponse>> getAvailableSlots(
			@Parameter(description = "룸 ID", required = true) @RequestParam Long roomId,
			@Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @RequestParam String date,
			ServerHttpRequest req
	) {
		log.info("예약 가능 슬롯 조회: roomId={}, date={}", roomId, date);
		
		return roomReservationClient.getAvailableSlots(roomId, date)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 다중 슬롯 예약
	 * POST /bff/v1/room-reservations/multi
	 */
	@PostMapping("/multi")
	@Operation(summary = "다중 슬롯 예약", description = "특정 날짜의 여러 시간 슬롯을 한 번에 예약 대기 상태로 변경합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "예약 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "401", description = "전화번호 등록이 필요합니다"),
			@ApiResponse(responseCode = "404", description = "슬롯을 찾을 수 없음"),
			@ApiResponse(responseCode = "409", description = "슬롯이 이미 예약됨 또는 예약 불가능")
	})
	public Mono<ResponseEntity<BaseResponse>> reserveMultipleSlots(
			@RequestBody MultiReservationRequest request,
			ServerHttpRequest req
	) {
		// JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
		String userId = req.getHeaders().getFirst("X-User-Id");
		
		log.info("다중 슬롯 예약 요청: userId={}, roomId={}, date={}, slotCount={}",
				userId, request.roomId(), request.slotDate(), request.slotTimes().size());
		
		// 사용자의 전화번호 등록 여부 확인
		return authClient.hasPhoneNumber(userId)
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
					return roomReservationClient.reserveMultipleSlots(request)
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
