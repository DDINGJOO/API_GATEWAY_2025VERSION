package com.study.api_gateway.controller.reservationManage;

import com.study.api_gateway.client.CouponClient;
import com.study.api_gateway.client.YeYakManageClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.coupon.request.CouponApplyRequest;
import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import com.study.api_gateway.dto.reservationManage.request.ReservationCreateRequest;
import com.study.api_gateway.dto.reservationManage.request.UserInfoUpdateRequest;
import com.study.api_gateway.service.ReservationEnrichmentService;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

import java.util.Set;

/**
 * 예약 관리 API
 * 예약 생성, 조회, 일간/주간/월간 조회, 사용자별 조회 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Management", description = "예약 관리 API")
public class ReservationManageController {
	
	private final YeYakManageClient yeYakManageClient;
	private final CouponClient couponClient;
	private final ReservationEnrichmentService reservationEnrichmentService;
	private final ResponseFactory responseFactory;
	
	/**
	 * 예약 생성 (예약자 정보 업데이트)
	 * POST /bff/v1/reservations
	 */
	@PostMapping
	@Operation(summary = "예약 생성", description = "예약 ID 기반으로 예약자 정보(이름, 전화번호)를 업데이트합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "생성 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ReservationCreateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"reservationId\": 123456,\n    \"message\": \"예약자 정보가 업데이트되었습니다.\"\n  },\n  \"request\": {\n    \"method\": \"POST\",\n    \"path\": \"/bff/v1/reservations\"\n  }\n}"))),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	@RequestBody(content = @Content(mediaType = "application/json",
			examples = @ExampleObject(name = "ReservationCreateRequest", value = "{\n  \"reservationId\": 123456,\n  \"reserverName\": \"홍길동\",\n  \"reserverPhone\": \"010-1234-5678\"\n}")))
	public Mono<ResponseEntity<BaseResponse>> createReservation(
			@org.springframework.web.bind.annotation.RequestBody ReservationCreateRequest request,
			ServerHttpRequest req
	) {
		log.info("예약 생성: reservationId={}, reserverName={}", request.getReservationId(), request.getReserverName());
		
		return yeYakManageClient.createReservation(request)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 예약 사용자 정보 업데이트 (예약 생성 2단계)
	 * POST /bff/v1/reservations/{reservationId}/user-info
	 * <p>
	 * 사용자 정보와 함께 쿠폰 적용을 처리합니다.
	 * 쿠폰이 제공된 경우, 쿠폰 적용 가능 여부를 확인한 후 예약 정보를 업데이트합니다.
	 */
	@PostMapping("/{reservationId}/user-info")
	@Operation(summary = "예약 사용자 정보 업데이트 (2단계)",
			description = "예약 ID 기반으로 사용자 정보를 업데이트하고 쿠폰을 적용합니다. AWAITING_USER_INFO 상태의 예약을 PENDING 상태로 전환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 또는 쿠폰 적용 실패"),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> updateUserInfo(
			@Parameter(description = "예약 ID", required = true)
			@PathVariable("reservationId") Long reservationId,
			@org.springframework.web.bind.annotation.RequestBody UserInfoUpdateRequest request,
			ServerHttpRequest req
	) {
		log.info("예약 사용자 정보 업데이트: reservationId={}, userId={}, couponId={}",
				reservationId, request.getUserId(), request.getCouponId());
		
		// 쿠폰이 제공된 경우 쿠폰 적용 처리
		if (request.getCouponId() != null && request.getRoomId() != null && request.getPlaceId() != null) {
			// 쿠폰 적용 요청 생성
			CouponApplyRequest couponApplyRequest = CouponApplyRequest.builder()
					.reservationId(String.valueOf(reservationId))
					.userId(request.getUserId())
					.couponId(request.getCouponId())
					.orderAmount(null) // 필요시 금액 추가
					.build();
			
			// 쿠폰 적용 후 사용자 정보 업데이트
			return couponClient.applyCoupon(couponApplyRequest)
					.flatMap(couponResponse -> {
						log.info("쿠폰 적용 성공: reservationId={}, couponResponse={}", reservationId, couponResponse);
						
						// 쿠폰 정보를 UserInfoUpdateRequest에 설정
						UserInfoUpdateRequest.CouponInfo couponInfo = UserInfoUpdateRequest.CouponInfo.builder()
								.couponId(String.valueOf(request.getCouponId()))
								.couponName(couponResponse.getCouponName())
								.discountType(couponResponse.getDiscountType() != null ? couponResponse.getDiscountType().toString() : null)
								.discountValue(couponResponse.getDiscountValue())
								.maxDiscountAmount(couponResponse.getMaxDiscountAmount())
								.build();
						
						request.setCouponInfo(couponInfo);
						
						// YeYakManage 서버로 사용자 정보 업데이트 요청
						return yeYakManageClient.updateUserInfo(reservationId, request)
								.map(response -> responseFactory.ok(response, req));
					})
					.onErrorResume(error -> {
						log.error("쿠폰 적용 실패: reservationId={}, error={}", reservationId, error.getMessage());
						// 쿠폰 적용 실패시 400 에러 반환
						return Mono.just(responseFactory.error(
								"쿠폰 적용에 실패했습니다: " + error.getMessage(),
								HttpStatus.BAD_REQUEST,
								req));
					});
		} else {
			// 쿠폰 없이 사용자 정보만 업데이트
			return yeYakManageClient.updateUserInfo(reservationId, request)
					.map(response -> responseFactory.ok(response, req))
					.onErrorResume(error -> {
						log.error("사용자 정보 업데이트 실패: reservationId={}, error={}", reservationId, error.getMessage());
						return Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, req));
					});
		}
	}
	
	/**
	 * 예약 상세 조회
	 * GET /bff/v1/reservations/detail/{id}
	 */
	@GetMapping("/detail/{id}")
	@Operation(summary = "예약 상세 조회", description = "예약 ID로 예약 상세 정보를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ReservationDetailSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"reservationId\": 123456,\n    \"userId\": 1001,\n    \"placeId\": 100,\n    \"roomId\": 10,\n    \"status\": \"CONFIRMED\",\n    \"totalPrice\": 72000,\n    \"reservationTimePrice\": 60000,\n    \"depositPrice\": 10000,\n    \"reviewId\": null,\n    \"reservationDate\": \"2025-01-15\",\n    \"reserverName\": \"홍길동\",\n    \"reserverPhone\": \"010-1234-5678\",\n    \"approvedAt\": \"2025-01-15T10:30:00\",\n    \"approvedBy\": 0,\n    \"createdAt\": \"2025-01-15T10:00:00\",\n    \"updatedAt\": \"2025-01-15T10:30:00\"\n  },\n  \"request\": {\n    \"method\": \"GET\",\n    \"path\": \"/bff/v1/reservations/123456\"\n  }\n}"))),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> getReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long id,
			ServerHttpRequest req
	) {
		log.info("예약 상세 조회: reservationId={}", id);
		
		return yeYakManageClient.getReservationById(id)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 내 예약 목록 조회 (커서 페이징)
	 * GET /bff/v1/reservations/me?cursor={cursor}&size={size}&statuses={statuses}
	 */
	@GetMapping("/me")
	@Operation(summary = "내 예약 목록 조회", description = "로그인한 사용자의 예약 목록을 커서 기반 페이징으로 조회합니다. 최신 예약이 먼저 표시됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (size는 1-100)"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	public Mono<ResponseEntity<BaseResponse>> getMyReservations(
			@Parameter(description = "커서 (Base64 인코딩, 다음 페이지 조회용)") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기 (1-100)", example = "20") @RequestParam(required = false, defaultValue = "20") Integer size,
			@Parameter(description = "상태 필터 (다중 선택 가능)", example = "CONFIRMED,PENDING")
			@RequestParam(required = false) Set<ReservationStatus> statuses,
			ServerHttpRequest req
	) {
		// JWT 필터에서 추가한 X-User-Id 헤더에서 userId 추출
		String userIdStr = req.getHeaders().getFirst("X-User-Id");
		if (userIdStr == null || userIdStr.isEmpty()) {
			log.warn("X-User-Id header is missing or empty in /reservations/me request");
			return Mono.just(responseFactory.error("인증이 필요합니다.", HttpStatus.UNAUTHORIZED, req));
		}
		
		Long userId = Long.parseLong(userIdStr);
		log.info("내 예약 목록 조회: userId={}, cursor={}, size={}, statuses={}",
				userId, cursor, size, statuses);
		
		return yeYakManageClient.getUserReservations(userId, cursor, size, statuses)
				.flatMap(reservationEnrichmentService::enrichUserReservations)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 결제 취소 (승인 전)
	 * POST /bff/v1/reservations/{id}/cancel
	 * PENDING_CONFIRMED 상태의 예약에 대해 결제 취소 요청
	 */
	@PostMapping("/{id}/cancel")
	@Operation(summary = "결제 취소 (승인 전)", description = "PENDING_CONFIRMED 상태의 예약에 대해 결제를 취소합니다. 취소 성공 시 즉시 REFUNDED 상태로 변경됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "취소 성공"),
			@ApiResponse(responseCode = "400", description = "취소 불가능한 상태"),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> cancelPayment(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long id,
			ServerHttpRequest req
	) {
		log.info("결제 취소 요청: reservationId={}", id);
		
		return yeYakManageClient.cancelPayment(id)
				.then(Mono.fromCallable(() -> responseFactory.ok("결제가 취소되었습니다.", req)))
				.onErrorResume(error -> {
					log.error("결제 취소 실패: reservationId={}, error={}", id, error.getMessage());
					return Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, req));
				});
	}

	/**
	 * 환불 요청 (승인 후)
	 * POST /bff/v1/reservations/{id}/refund
	 * CONFIRMED 또는 REJECTED 상태의 예약에 대해 환불 요청
	 */
	@PostMapping("/{id}/refund")
	@Operation(summary = "환불 요청 (승인 후)", description = "CONFIRMED 또는 REJECTED 상태의 예약에 대해 환불을 요청합니다. 실제 상태 변경은 결제 서버의 환불 완료 이벤트 수신 시 처리됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "환불 요청 성공"),
			@ApiResponse(responseCode = "400", description = "환불 불가능한 상태"),
			@ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> refundReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long id,
			ServerHttpRequest req
	) {
		log.info("환불 요청: reservationId={}", id);
		
		return yeYakManageClient.refundReservation(id)
				.then(Mono.fromCallable(() -> responseFactory.ok("환불 요청이 접수되었습니다.", req)))
				.onErrorResume(error -> {
					log.error("환불 요청 실패: reservationId={}, error={}", id, error.getMessage());
					return Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, req));
				});
	}
}
