package com.study.api_gateway.api.coupon.controller;

import com.study.api_gateway.api.coupon.dto.enums.CouponStatus;
import com.study.api_gateway.api.coupon.dto.request.*;
import com.study.api_gateway.api.coupon.service.CouponFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.common.util.UserIdValidator;
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
public class CouponController implements CouponApi {
	private final CouponFacadeService couponFacadeService;
	private final ResponseFactory responseFactory;
	private final UserIdValidator userIdValidator;
	
	@Override
	@PostMapping("/policies")
	public Mono<ResponseEntity<BaseResponse>> createCouponPolicy(
			@RequestBody CouponPolicyCreateRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 생성 시작] couponName: {}, couponCode: {}",
				request.getCouponName(), request.getCouponCode());
		
		return couponFacadeService.createCouponPolicy(request)
				.doOnSuccess(response -> log.info("[쿠폰 정책 생성 성공] policyId: {}", response.getId()))
				.doOnError(error -> log.error("[쿠폰 정책 생성 실패] error: {}", error.getMessage()))
				.map(response -> responseFactory.ok(response, httpRequest, HttpStatus.CREATED))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/policies/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> getCouponPolicy(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 조회] policyId: {}", policyId);
		
		return couponFacadeService.getCouponPolicy(policyId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.NOT_FOUND, httpRequest)));
	}
	
	@Override
	@PatchMapping("/policies/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> updateCouponPolicy(
			@PathVariable Long policyId,
			@RequestBody CouponPolicyCreateRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 수정] policyId: {}", policyId);
		
		return couponFacadeService.updateCouponPolicy(policyId, request)
				.doOnSuccess(response -> log.info("[쿠폰 정책 수정 성공] policyId: {}", policyId))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@DeleteMapping("/policies/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> deleteCouponPolicy(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 비활성화] policyId: {}", policyId);
		
		return couponFacadeService.deleteCouponPolicy(policyId)
				.then(Mono.just(responseFactory.ok(null, httpRequest, HttpStatus.NO_CONTENT)))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@PatchMapping("/policies/{policyId}/remaining-quantity")
	public Mono<ResponseEntity<BaseResponse>> updatePolicyQuantity(
			@PathVariable Long policyId,
			@RequestBody PolicyQuantityUpdateRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 정책 재고 수정] policyId: {}, newMaxIssueCount: {}",
				policyId, request.getNewMaxIssueCount());
		
		return couponFacadeService.updatePolicyQuantity(policyId, request)
				.doOnSuccess(response -> log.info("[재고 수정 성공] policyId: {}, remainingCount: {}",
						policyId, response.getRemainingCount()))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@PostMapping("/download")
	public Mono<ResponseEntity<BaseResponse>> downloadCoupon(
			@RequestBody CouponDownloadRequest request,
			ServerHttpRequest httpRequest) {
		
		String userId = userIdValidator.extractTokenUserId(httpRequest);
		request.setUserId(Long.valueOf(userId));
		
		log.info("[쿠폰 다운로드] userId: {}, couponCode: {}", userId, request.getCouponCode());
		
		return couponFacadeService.downloadCoupon(request)
				.doOnSuccess(response -> log.info("[쿠폰 다운로드 성공] userId: {}, couponId: {}",
						userId, response.getCouponId()))
				.map(response -> responseFactory.ok(response, httpRequest, HttpStatus.CREATED))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/validate/{couponCode}")
	public Mono<ResponseEntity<BaseResponse>> validateCouponCode(
			@PathVariable String couponCode,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 코드 유효성 확인] couponCode: {}", couponCode);
		
		return couponFacadeService.validateCouponCode(couponCode)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@PostMapping("/direct-issue")
	public Mono<ResponseEntity<BaseResponse>> issueDirectCoupon(
			@RequestBody CouponDirectIssueRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[직접 쿠폰 발급] couponPolicyId: {}, userCount: {}",
				request.getCouponPolicyId(), request.getUserIds().size());
		
		return couponFacadeService.issueDirectCoupon(request)
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
	
	@Override
	@PostMapping("/issue/fcfs")
	public Mono<ResponseEntity<BaseResponse>> issueFcfsCoupon(
			@RequestBody FcfsIssueRequest request,
			ServerHttpRequest httpRequest) {
		
		String userId = userIdValidator.extractTokenUserId(httpRequest);
		
		log.info("[선착순 쿠폰 발급] userId: {}, policyId: {}", userId, request.getPolicyId());
		
		return couponFacadeService.issueFcfsCoupon(request.getPolicyId(), Long.valueOf(userId))
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
	
	@Override
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
		
		return couponFacadeService.getUserCoupons(userId, status, productIds, cursor, limit)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/users/{userId}/expiring")
	public Mono<ResponseEntity<BaseResponse>> getExpiringCoupons(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "7") Integer days,
			@RequestParam(defaultValue = "10") Integer limit,
			ServerHttpRequest httpRequest) {
		
		log.info("[만료 임박 쿠폰 조회] userId: {}, days: {}, limit: {}", userId, days, limit);
		
		return couponFacadeService.getExpiringCoupons(userId, days, limit)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/users/{userId}/statistics")
	public Mono<ResponseEntity<BaseResponse>> getUserCouponStatistics(
			@PathVariable Long userId,
			ServerHttpRequest httpRequest) {
		
		log.info("[사용자 쿠폰 통계 조회] userId: {}", userId);
		
		return couponFacadeService.getUserCouponStatistics(userId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/users/me")
	public Mono<ResponseEntity<BaseResponse>> getUserCouponsLegacy(
			@RequestParam(required = false) CouponStatus status,
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "20") Integer size,
			@RequestParam(defaultValue = "issuedAt,desc") String sort,
			ServerHttpRequest httpRequest) {
		
		String userId = userIdValidator.extractTokenUserId(httpRequest);
		
		log.info("[쿠폰 목록 조회 (레거시)] userId: {}, status: {}, page: {}", userId, status, page);
		
		return couponFacadeService.getUserCouponsLegacy(Long.valueOf(userId), status, page, size, sort)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@PostMapping("/reserve")
	public Mono<ResponseEntity<BaseResponse>> reserveCoupon(
			@RequestBody CouponReserveRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 예약] reservationId: {}, userId: {}, couponId: {}",
				request.getReservationId(), request.getUserId(), request.getCouponId());
		
		return couponFacadeService.reserveCoupon(request)
				.doOnSuccess(response -> log.info("[쿠폰 예약 성공] reservationId: {}",
						response.getReservationId()))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@PostMapping("/apply")
	public Mono<ResponseEntity<BaseResponse>> applyCoupon(
			@RequestBody CouponApplyRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 적용] reservationId: {}, userId: {}, couponId: {}",
				request.getReservationId(), request.getUserId(), request.getCouponId());
		
		return couponFacadeService.applyCoupon(request)
				.map(response -> responseFactory.ok(response, httpRequest))
				.switchIfEmpty(Mono.just(responseFactory.ok(null, httpRequest, HttpStatus.NO_CONTENT)))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@DeleteMapping("/apply/{reservationId}")
	public Mono<ResponseEntity<BaseResponse>> unlockCoupon(
			@PathVariable String reservationId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 락 해제] reservationId: {}", reservationId);
		
		return couponFacadeService.unlockCoupon(reservationId)
				.then(Mono.just(responseFactory.ok(null, httpRequest)))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@PostMapping("/use")
	public Mono<ResponseEntity<BaseResponse>> useCoupon(
			@RequestBody CouponUseRequest request,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 사용 확정] reservationId: {}, paymentId: {}",
				request.getReservationId(), request.getPaymentId());
		
		return couponFacadeService.useCoupon(request)
				.doOnSuccess(response -> log.info("[쿠폰 사용 완료] couponId: {}",
						response.getCouponId()))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@DeleteMapping("/reserve/{reservationId}")
	public Mono<ResponseEntity<BaseResponse>> cancelReservation(
			@PathVariable String reservationId,
			ServerHttpRequest httpRequest) {
		
		log.info("[쿠폰 예약 취소] reservationId: {}", reservationId);
		
		return couponFacadeService.cancelReservation(reservationId)
				.doOnSuccess(response -> log.info("[예약 취소 완료] reservationId: {}",
						reservationId))
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/statistics/realtime/{policyId}")
	public Mono<ResponseEntity<BaseResponse>> getRealtimeStatistics(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest) {
		
		log.info("[실시간 통계 조회] policyId: {}", policyId);
		
		return couponFacadeService.getRealtimeStatistics(policyId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/statistics/global")
	public Mono<ResponseEntity<BaseResponse>> getGlobalStatistics(ServerHttpRequest httpRequest) {
		
		log.info("[전체 통계 조회]");
		
		return couponFacadeService.getGlobalStatistics()
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/statistics/user/{userId}")
	public Mono<ResponseEntity<BaseResponse>> getUserStatistics(
			@PathVariable Long userId,
			ServerHttpRequest httpRequest) {
		
		log.info("[사용자 통계 조회] userId: {}", userId);
		
		return couponFacadeService.getUserStatistics(userId)
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
	
	@Override
	@GetMapping("/statistics/dashboard")
	public Mono<ResponseEntity<BaseResponse>> getDashboardStatistics(ServerHttpRequest httpRequest) {
		
		log.info("[대시보드 통계 조회]");
		
		return couponFacadeService.getDashboardStatistics()
				.map(response -> responseFactory.ok(response, httpRequest))
				.onErrorResume(error -> Mono.just(responseFactory.error(error.getMessage(), HttpStatus.BAD_REQUEST, httpRequest)));
	}
}
