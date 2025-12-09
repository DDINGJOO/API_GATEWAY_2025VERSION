package com.study.api_gateway.controller.coupon;

import com.study.api_gateway.client.CouponClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.coupon.enums.CouponStatus;
import com.study.api_gateway.dto.coupon.request.*;
import com.study.api_gateway.dto.coupon.response.CouponIssueResponse;
import com.study.api_gateway.dto.coupon.response.CouponPolicyResponse;
import com.study.api_gateway.util.ResponseFactory;
import com.study.api_gateway.util.UserIdValidator;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 쿠폰 서비스 API Gateway 컨트롤러
 */
@RestController
@RequestMapping("/bff/v1/coupons")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public class CouponController {
	private final CouponClient couponClient;
	private final ResponseFactory responseFactory;
	private final UserIdValidator userIdValidator;
	
	/**
	 * 쿠폰 정책 생성 (관리자용)
	 */
	@Operation(summary = "쿠폰 정책 생성", description = "관리자가 새로운 쿠폰 정책을 생성합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "쿠폰 정책 생성 성공",
					content = @Content(schema = @Schema(implementation = CouponPolicyResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증 실패"),
			@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@PostMapping("/policies")
	public Mono<ResponseEntity<BaseResponse>> createCouponPolicy(
			@RequestBody CouponPolicyCreateRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 생성 시작] couponName: {}, couponCode: {}",
				request.getCouponName(), request.getCouponCode());
		
		return couponClient.createCouponPolicy(request)
				.doOnSuccess(response -> log.info("[쿠폰 정책 생성 성공] policyId: {}", response.getId()))
				.doOnError(error -> log.error("[쿠폰 정책 생성 실패] error: {}", error.getMessage()))
				.map(response -> responseFactory.ok(response, httpRequest, HttpStatus.CREATED))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 정책 조회
	 */
	@Operation(summary = "쿠폰 정책 조회", description = "특정 쿠폰 정책을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(schema = @Schema(implementation = CouponPolicyResponse.class))),
			@ApiResponse(responseCode = "404", description = "쿠폰 정책을 찾을 수 없음")
	})
	@GetMapping("/policies/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> getCouponPolicy(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 조회] policyId: {}", policyId);
		
		return couponClient.getCouponPolicy(policyId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.NOT_FOUND, httpRequest)));
	}
	
	/**
	 * 쿠폰 정책 수정 (관리자용)
	 */
	@Operation(summary = "쿠폰 정책 수정", description = "기존 쿠폰 정책을 수정합니다")
	@PatchMapping("/policies/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> updateCouponPolicy(
			@PathVariable Long policyId,
			@RequestBody CouponPolicyCreateRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 수정] policyId: {}", policyId);
		
		return couponClient.updateCouponPolicy(policyId, request)
				.doOnSuccess(response -> log.info("[쿠폰 정책 수정 성공] policyId: {}", policyId))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 정책 비활성화 (관리자용)
	 */
	@Operation(summary = "쿠폰 정책 비활성화", description = "쿠폰 정책을 비활성화합니다")
	@DeleteMapping("/policies/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> deleteCouponPolicy(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 비활성화] policyId: {}", policyId);
		
		return couponClient.deleteCouponPolicy(policyId)
				.then(Mono.just(responseFactory.ok(null, httpRequest, HttpStatus.NO_CONTENT)))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 정책 남은 발급 수량 수정 (관리자용)
	 */
	@Operation(summary = "쿠폰 정책 재고 수정", description = "쿠폰 정책의 남은 발급 수량을 수정합니다")
	@PatchMapping("/policies/{policyId}/remaining-quantity")
	public Mono<ResponseEntity<BaseResponse>> updatePolicyQuantity(
			@PathVariable Long policyId,
			@RequestBody PolicyQuantityUpdateRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 재고 수정] policyId: {}, newMaxIssueCount: {}",
				policyId, request.getNewMaxIssueCount());
		
		return couponClient.updatePolicyQuantity(policyId, request)
				.doOnSuccess(response -> log.info("[재고 수정 성공] policyId: {}, remainingCount: {}",
						policyId, response.getRemainingCount()))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 다운로드 (코드로 발급)
	 */
	@Operation(summary = "쿠폰 다운로드", description = "쿠폰 코드를 사용하여 쿠폰을 다운로드합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "발급 성공",
					content = @Content(schema = @Schema(implementation = CouponIssueResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 쿠폰 코드"),
			@ApiResponse(responseCode = "409", description = "이미 발급받은 쿠폰")
	})
	@PostMapping("/download")
	public Mono<ResponseEntity<BaseResponse>> downloadCoupon(
			@RequestBody CouponDownloadRequest request,
			ServerHttpRequest httpRequest) {
		
		String userId = userIdValidator.extractTokenUserId(httpRequest);
		request.setUserId(Long.valueOf(userId));
		
		log.info("[쿠폰 다운로드] userId: {}, couponCode: {}", userId, request.getCouponCode());
		
		return couponClient.downloadCoupon(request)
				.doOnSuccess(response -> log.info("[쿠폰 다운로드 성공] userId: {}, couponId: {}",
						userId, response.getCouponId()))
				.map(response -> responseFactory.ok(response, httpRequest, HttpStatus.CREATED))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 코드 유효성 확인
	 */
	@Operation(summary = "쿠폰 코드 유효성 확인", description = "쿠폰 코드의 유효성을 확인합니다")
	@GetMapping("/validate/{couponCode}")
	public Mono<ResponseEntity<BaseResponse>> validateCouponCode(
			@PathVariable String couponCode,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 코드 유효성 확인] couponCode: {}", couponCode);
		
		return couponClient.validateCouponCode(couponCode)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 직접 발급 (관리자용)
	 */
	@Operation(summary = "직접 쿠폰 발급", description = "관리자가 특정 사용자들에게 쿠폰을 직접 발급합니다")
	@PostMapping("/direct-issue")
	public Mono<ResponseEntity<BaseResponse>> issueDirectCoupon(
			@RequestBody CouponDirectIssueRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[직접 쿠폰 발급] couponPolicyId: {}, userCount: {}",
				request.getCouponPolicyId(), request.getUserIds().size());
		
		return couponClient.issueDirectCoupon(request)
				.doOnSuccess(response -> log.info("[직접 발급 완료] 성공: {}, 실패: {}",
						response.getSuccessCount(), response.getFailureCount()))
				.map(response -> {
					// 부분 성공인 경우 207 Multi-Status 반환
					if (response.getFailureCount() > 0 && response.getSuccessCount() > 0) {
						return responseFactory.ok(response, httpRequest, HttpStatus.MULTI_STATUS);
					}
					return responseFactory.ok(response, httpRequest, HttpStatus.CREATED);
				})
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 선착순 쿠폰 발급
	 */
	@Operation(summary = "선착순 쿠폰 발급", description = "선착순으로 쿠폰을 발급받습니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "발급 성공"),
			@ApiResponse(responseCode = "409", description = "재고 소진")
	})
	@PostMapping("/issue/fcfs")
	public Mono<ResponseEntity<BaseResponse>> issueFcfsCoupon(
			@RequestBody FcfsIssueRequest request,
			ServerHttpRequest httpRequest) {
		
		String userId = userIdValidator.extractTokenUserId(httpRequest);
		
		log.info("[선착순 쿠폰 발급] userId: {}, policyId: {}", userId, request.getPolicyId());
		
		return couponClient.issueFcfsCoupon(request.getPolicyId(), Long.valueOf(userId))
				.doOnSuccess(response -> log.info("[선착순 발급 성공] userId: {}, couponId: {}",
						userId, response.getCouponId()))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> {
					if (error.getMessage().contains("STOCK_EXHAUSTED")) {
						return Mono.just(responseFactory.error("쿠폰 재고가 모두 소진되었습니다", HttpStatus.CONFLICT, httpRequest));
					}
					return Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest));
				});
	}
	
	/**
	 * 사용자 쿠폰 목록 조회 (커서 기반)
	 */
	@Operation(summary = "쿠폰 목록 조회", description = "사용자의 쿠폰 목록을 커서 기반으로 조회합니다")
	@GetMapping("/users/{userId}")
	public Mono<ResponseEntity<BaseResponse>> getUserCoupons(
			@PathVariable Long userId,
			@RequestParam(required = false) CouponStatus status,
			@RequestParam(required = false) List<Long> productIds,
			@RequestParam(required = false) Long cursor,
			@RequestParam(defaultValue = "20") Integer limit,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 목록 조회] userId: {}, status: {}, cursor: {}, limit: {}",
				userId, status, cursor, limit);
		
		return couponClient.getUserCoupons(userId, status, productIds, cursor, limit)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 만료 임박 쿠폰 조회
	 */
	@Operation(summary = "만료 임박 쿠폰 조회", description = "만료가 임박한 쿠폰 목록을 조회합니다")
	@GetMapping("/users/{userId}/expiring")
	public Mono<ResponseEntity<BaseResponse>> getExpiringCoupons(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "7") Integer days,
			@RequestParam(defaultValue = "10") Integer limit,
			ServerHttpRequest httpRequest) {
		
		log.info("[만료 임박 쿠폰 조회] userId: {}, days: {}, limit: {}", userId, days, limit);
		
		return couponClient.getExpiringCoupons(userId, days, limit)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 사용자 쿠폰 통계
	 */
	@Operation(summary = "사용자 쿠폰 통계", description = "사용자의 쿠폰 사용 통계를 조회합니다")
	@GetMapping("/users/{userId}/statistics")
	public Mono<ResponseEntity<BaseResponse>> getUserCouponStatistics(
			@PathVariable Long userId,
			ServerHttpRequest httpRequest) {
		
		log.info("[사용자 쿠폰 통계 조회] userId: {}", userId);
		
		return couponClient.getUserCouponStatistics(userId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 사용자 쿠폰 목록 조회 (레거시)
	 */
	@Operation(summary = "내 쿠폰 목록 조회 (레거시)", description = "사용자가 보유한 쿠폰 목록을 조회합니다 (페이지네이션)")
	@GetMapping("/users/me")
	public Mono<ResponseEntity<BaseResponse>> getUserCouponsLegacy(
			@RequestParam(required = false) CouponStatus status,
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "20") Integer size,
			@RequestParam(defaultValue = "issuedAt,desc") String sort,
			ServerHttpRequest httpRequest) {
		
		String userId = userIdValidator.extractTokenUserId(httpRequest);
		
		log.info("[쿠폰 목록 조회 (레거시)] userId: {}, status: {}, page: {}", userId, status, page);
		
		return couponClient.getUserCouponsLegacy(Long.valueOf(userId), status, page, size, sort)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 예약
	 */
	@Operation(summary = "쿠폰 예약", description = "주문 시 쿠폰을 예약합니다")
	@PostMapping("/reserve")
	public Mono<ResponseEntity<BaseResponse>> reserveCoupon(
			@RequestBody CouponReserveRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 예약] reservationId: {}, userId: {}, couponId: {}",
				request.getReservationId(), request.getUserId(), request.getCouponId());
		
		return couponClient.reserveCoupon(request)
				.doOnSuccess(response -> log.info("[쿠폰 예약 성공] reservationId: {}",
						response.getReservationId()))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 적용 (상품별 쿠폰 적용 가능 여부 확인)
	 */
	@Operation(summary = "쿠폰 적용", description = "쿠폰 적용 가능 여부를 확인하고 적용합니다")
	@PostMapping("/apply")
	public Mono<ResponseEntity<BaseResponse>> applyCoupon(
			@RequestBody CouponApplyRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 적용] reservationId: {}, userId: {}, couponId: {}",
				request.getReservationId(), request.getUserId(), request.getCouponId());
		
		return couponClient.applyCoupon(request)
				.map(response -> responseFactory.ok(response, httpRequest))
				.switchIfEmpty(Mono.just(responseFactory.ok(null, httpRequest, HttpStatus.NO_CONTENT)))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 락 해제
	 */
	@Operation(summary = "쿠폰 락 해제", description = "적용된 쿠폰의 락을 해제합니다")
	@DeleteMapping("/apply/{reservationId}")
	public Mono<ResponseEntity<BaseResponse>> unlockCoupon(
			@PathVariable String reservationId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 락 해제] reservationId: {}", reservationId);
		
		return couponClient.unlockCoupon(reservationId)
				.then(Mono.just(responseFactory.ok(null, httpRequest)))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 사용 확정
	 */
	@Operation(summary = "쿠폰 사용 확정", description = "결제 완료 후 쿠폰 사용을 확정합니다")
	@PostMapping("/use")
	public Mono<ResponseEntity<BaseResponse>> useCoupon(
			@RequestBody CouponUseRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 사용 확정] reservationId: {}, paymentId: {}",
				request.getReservationId(), request.getPaymentId());
		
		return couponClient.useCoupon(request)
				.doOnSuccess(response -> log.info("[쿠폰 사용 완료] couponId: {}",
						response.getCouponId()))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 쿠폰 예약 취소
	 */
	@Operation(summary = "쿠폰 예약 취소", description = "예약된 쿠폰을 취소합니다")
	@DeleteMapping("/reserve/{reservationId}")
	public Mono<ResponseEntity<BaseResponse>> cancelReservation(
			@PathVariable String reservationId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 예약 취소] reservationId: {}", reservationId);
		
		return couponClient.cancelReservation(reservationId)
				.doOnSuccess(response -> log.info("[예약 취소 완료] reservationId: {}",
						reservationId))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 실시간 통계 조회
	 */
	@Operation(summary = "실시간 쿠폰 통계", description = "특정 쿠폰 정책의 실시간 통계를 조회합니다")
	@GetMapping("/statistics/realtime/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> getRealtimeStatistics(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest) {
		
		log.info("[실시간 통계 조회] policyId: {}", policyId);
		
		return couponClient.getRealtimeStatistics(policyId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 전체 통계 조회
	 */
	@Operation(summary = "전체 쿠폰 통계", description = "전체 쿠폰 시스템 통계를 조회합니다")
	@GetMapping("/statistics/global")
	public Mono<ResponseEntity<BaseResponse>> getGlobalStatistics(ServerHttpRequest httpRequest) {
		
		log.info("[전체 통계 조회]");
		
		return couponClient.getGlobalStatistics()
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 사용자 통계 조회
	 */
	@Operation(summary = "사용자 쿠폰 통계", description = "특정 사용자의 쿠폰 사용 통계를 조회합니다")
	@GetMapping("/statistics/user/{userId}")
	public Mono<ResponseEntity<BaseResponse>> getUserStatistics(
			@PathVariable Long userId,
			ServerHttpRequest httpRequest) {
		
		log.info("[사용자 통계 조회] userId: {}", userId);
		
		return couponClient.getUserStatistics(userId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 대시보드 통계 조회
	 */
	
	@Operation(summary = "대시보드 통계", description = "대시보드용 쿠폰 통계 요약을 조회합니다")
	@GetMapping("/statistics/dashboard")
	public Mono<ResponseEntity<BaseResponse>> getDashboardStatistics(ServerHttpRequest httpRequest) {
		
		log.info("[대시보드 통계 조회]");
		
		return couponClient.getDashboardStatistics()
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	/**
	 * 선착순 발급 요청 DTO
	 */
	@lombok.Data
	@lombok.NoArgsConstructor
	@lombok.AllArgsConstructor
	static class FcfsIssueRequest {
		private Long policyId;
		private Long userId;
	}
}
