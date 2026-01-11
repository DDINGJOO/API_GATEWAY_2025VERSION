package com.study.api_gateway.api.reservationManage.controller;

import com.study.api_gateway.api.reservationManage.dto.enums.ReservationStatus;
import com.study.api_gateway.api.reservationManage.dto.request.ReservationCreateRequest;
import com.study.api_gateway.api.reservationManage.dto.request.UserInfoUpdateRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 예약 관리 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Reservation Management", description = "예약 관리 API")
public interface ReservationManageApi {

	@Operation(summary = "예약 생성", description = "예약 ID 기반으로 예약자 정보(이름, 전화번호)를 업데이트합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "생성 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> createReservation(
			@RequestBody ReservationCreateRequest request,
			ServerHttpRequest req);

	@Operation(summary = "예약 사용자 정보 업데이트 (2단계)",
			description = "예약 ID 기반으로 사용자 정보를 업데이트하고 쿠폰을 적용합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 또는 쿠폰 적용 실패"),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	@PostMapping("/{reservationId}/user-info")
	Mono<ResponseEntity<BaseResponse>> updateUserInfo(
			@Parameter(description = "예약 ID", required = true)
			@PathVariable("reservationId") Long reservationId,
			@RequestBody UserInfoUpdateRequest request,
			ServerHttpRequest req);

	@Operation(summary = "예약 상세 조회", description = "예약 ID로 예약 상세 정보를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	@GetMapping("/detail/{id}")
	Mono<ResponseEntity<BaseResponse>> getReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long id,
			ServerHttpRequest req);

	@Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자의 예약 목록을 커서 기반 페이징으로 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@GetMapping("/me")
	Mono<ResponseEntity<BaseResponse>> getMyReservations(
			@Parameter(description = "커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "20") Integer size,
			@Parameter(description = "상태 필터")
			@RequestParam(required = false) Set<ReservationStatus> statuses,
			ServerHttpRequest req);

	@Operation(summary = "결제 취소 (승인 전)", description = "PENDING_CONFIRMED 상태의 예약에 대해 결제를 취소합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "취소 성공"),
			@ApiResponse(responseCode = "400", description = "취소 불가능한 상태"),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	@PostMapping("/{id}/cancel")
	Mono<ResponseEntity<BaseResponse>> cancelPayment(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long id,
			ServerHttpRequest req);

	@Operation(summary = "환불 요청 (승인 후)", description = "CONFIRMED 또는 REJECTED 상태의 예약에 대해 환불을 요청합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "환불 요청 성공"),
			@ApiResponse(responseCode = "400", description = "환불 불가능한 상태"),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	@PostMapping("/{id}/refund")
	Mono<ResponseEntity<BaseResponse>> refundReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long id,
			ServerHttpRequest req);
}
