package com.study.api_gateway.api.reservationManage.controller;

import com.study.api_gateway.api.reservationManage.controller.ReservationManageApi;
import com.study.api_gateway.api.coupon.service.CouponFacadeService;
import com.study.api_gateway.api.reservationManage.service.ReservationManageFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.api.coupon.dto.request.CouponApplyRequest;
import com.study.api_gateway.api.reservationManage.dto.enums.ReservationStatus;
import com.study.api_gateway.api.reservationManage.dto.request.ReservationCreateRequest;
import com.study.api_gateway.api.reservationManage.dto.request.UserInfoUpdateRequest;
import com.study.api_gateway.enrichment.ReservationEnrichmentService;
import com.study.api_gateway.common.response.ResponseFactory;
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
public class ReservationManageController implements ReservationManageApi {

	private final ReservationManageFacadeService reservationManageFacadeService;
	private final CouponFacadeService couponFacadeService;
	private final ReservationEnrichmentService reservationEnrichmentService;
	private final ResponseFactory responseFactory;

	/**
	 * 예약 생성 (예약자 정보 업데이트)
	 * POST /bff/v1/reservations
	 */
	@Override
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> createReservation(
			@RequestBody ReservationCreateRequest request,
			ServerHttpRequest req
	) {
		log.info("예약 생성: reservationId={}, reserverName={}", request.getReservationId(), request.getReserverName());

		return reservationManageFacadeService.createReservation(request)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 예약 사용자 정보 업데이트 (예약 생성 2단계)
	 * POST /bff/v1/reservations/{reservationId}/user-info
	 * <p>
	 * 사용자 정보와 함께 쿠폰 적용을 처리합니다.
	 * 쿠폰이 제공된 경우, 쿠폰 적용 가능 여부를 확인한 후 예약 정보를 업데이트합니다.
	 */
	@Override
	@PostMapping("/{reservationId}/user-info")
	public Mono<ResponseEntity<BaseResponse>> updateUserInfo(
			@PathVariable("reservationId") Long reservationId,
			@RequestBody UserInfoUpdateRequest request,
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
			return couponFacadeService.applyCoupon(couponApplyRequest)
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
						return reservationManageFacadeService.updateUserInfo(reservationId, request)
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
			return reservationManageFacadeService.updateUserInfo(reservationId, request)
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
	@Override
	@GetMapping("/detail/{id}")
	public Mono<ResponseEntity<BaseResponse>> getReservation(
			@PathVariable Long id,
			ServerHttpRequest req
	) {
		log.info("예약 상세 조회: reservationId={}", id);

		return reservationManageFacadeService.getReservationById(id)
				.flatMap(reservationEnrichmentService::enrichReservationDetail)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 내 예약 목록 조회 (커서 페이징)
	 * GET /bff/v1/reservations/me?cursor={cursor}&size={size}&statuses={statuses}
	 */
	@Override
	@GetMapping("/me")
	public Mono<ResponseEntity<BaseResponse>> getMyReservations(
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false, defaultValue = "20") Integer size,
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

		return reservationManageFacadeService.getUserReservations(userId, cursor, size, statuses)
				.flatMap(reservationEnrichmentService::enrichUserReservations)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 결제 취소 (승인 전)
	 * POST /bff/v1/reservations/{id}/cancel
	 * PENDING_CONFIRMED 상태의 예약에 대해 결제 취소 요청
	 */
	@Override
	@PostMapping("/{id}/cancel")
	public Mono<ResponseEntity<BaseResponse>> cancelPayment(
			@PathVariable Long id,
			ServerHttpRequest req
	) {
		log.info("결제 취소 요청: reservationId={}", id);

		return reservationManageFacadeService.cancelPayment(id)
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
	@Override
	@PostMapping("/{id}/refund")
	public Mono<ResponseEntity<BaseResponse>> refundReservation(
			@PathVariable Long id,
			ServerHttpRequest req
	) {
		log.info("환불 요청: reservationId={}", id);

		return reservationManageFacadeService.refundReservation(id)
				.then(Mono.fromCallable(() -> responseFactory.ok("환불 요청이 접수되었습니다.", req)))
				.onErrorResume(error -> {
					log.error("환불 요청 실패: reservationId={}, error={}", id, error.getMessage());
					return Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, req));
				});
	}
}
