package com.study.api_gateway.api.coupon.controller;

import com.study.api_gateway.api.coupon.dto.enums.CouponStatus;
import com.study.api_gateway.api.coupon.dto.request.*;
import com.study.api_gateway.api.coupon.dto.response.CouponIssueResponse;
import com.study.api_gateway.api.coupon.dto.response.CouponPolicyResponse;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 쿠폰 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public interface CouponApi {

	// ==================== 쿠폰 정책 관리 (관리자용) ====================

	@Operation(summary = "쿠폰 정책 생성", description = "관리자가 새로운 쿠폰 정책을 생성합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "쿠폰 정책 생성 성공",
					content = @Content(schema = @Schema(implementation = CouponPolicyResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "401", description = "인증 실패"),
			@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@PostMapping("/policies")
	Mono<ResponseEntity<BaseResponse>> createCouponPolicy(
			@RequestBody CouponPolicyCreateRequest request,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 정책 조회", description = "특정 쿠폰 정책을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(schema = @Schema(implementation = CouponPolicyResponse.class))),
			@ApiResponse(responseCode = "404", description = "쿠폰 정책을 찾을 수 없음")
	})
	@GetMapping("/policies/{policyId}")
	Mono<ResponseEntity<BaseResponse>> getCouponPolicy(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 정책 수정", description = "기존 쿠폰 정책을 수정합니다")
	@PatchMapping("/policies/{policyId}")
	Mono<ResponseEntity<BaseResponse>> updateCouponPolicy(
			@PathVariable Long policyId,
			@RequestBody CouponPolicyCreateRequest request,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 정책 비활성화", description = "쿠폰 정책을 비활성화합니다")
	@DeleteMapping("/policies/{policyId}")
	Mono<ResponseEntity<BaseResponse>> deleteCouponPolicy(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 정책 재고 수정", description = "쿠폰 정책의 남은 발급 수량을 수정합니다")
	@PatchMapping("/policies/{policyId}/remaining-quantity")
	Mono<ResponseEntity<BaseResponse>> updatePolicyQuantity(
			@PathVariable Long policyId,
			@RequestBody PolicyQuantityUpdateRequest request,
			ServerHttpRequest httpRequest);

	// ==================== 쿠폰 발급 ====================

	@Operation(summary = "쿠폰 다운로드", description = "쿠폰 코드를 사용하여 쿠폰을 다운로드합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "발급 성공",
					content = @Content(schema = @Schema(implementation = CouponIssueResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 쿠폰 코드"),
			@ApiResponse(responseCode = "409", description = "이미 발급받은 쿠폰")
	})
	@PostMapping("/download")
	Mono<ResponseEntity<BaseResponse>> downloadCoupon(
			@RequestBody CouponDownloadRequest request,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 코드 유효성 확인", description = "쿠폰 코드의 유효성을 확인합니다")
	@GetMapping("/validate/{couponCode}")
	Mono<ResponseEntity<BaseResponse>> validateCouponCode(
			@PathVariable String couponCode,
			ServerHttpRequest httpRequest);

	@Operation(summary = "직접 쿠폰 발급", description = "관리자가 특정 사용자들에게 쿠폰을 직접 발급합니다")
	@PostMapping("/direct-issue")
	Mono<ResponseEntity<BaseResponse>> issueDirectCoupon(
			@RequestBody CouponDirectIssueRequest request,
			ServerHttpRequest httpRequest);

	@Operation(summary = "선착순 쿠폰 발급", description = "선착순으로 쿠폰을 발급받습니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "발급 성공"),
			@ApiResponse(responseCode = "409", description = "재고 소진")
	})
	@PostMapping("/issue/fcfs")
	Mono<ResponseEntity<BaseResponse>> issueFcfsCoupon(
			@RequestBody FcfsIssueRequest request,
			ServerHttpRequest httpRequest);

	// ==================== 사용자 쿠폰 조회 ====================

	@Operation(summary = "쿠폰 목록 조회", description = "사용자의 쿠폰 목록을 커서 기반으로 조회합니다")
	@GetMapping("/users/{userId}")
	Mono<ResponseEntity<BaseResponse>> getUserCoupons(
			@PathVariable Long userId,
			@RequestParam(required = false) CouponStatus status,
			@RequestParam(required = false) List<Long> productIds,
			@RequestParam(required = false) Long cursor,
			@RequestParam(defaultValue = "20") Integer limit,
			ServerHttpRequest httpRequest);

	@Operation(summary = "만료 임박 쿠폰 조회", description = "만료가 임박한 쿠폰 목록을 조회합니다")
	@GetMapping("/users/{userId}/expiring")
	Mono<ResponseEntity<BaseResponse>> getExpiringCoupons(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "7") Integer days,
			@RequestParam(defaultValue = "10") Integer limit,
			ServerHttpRequest httpRequest);

	@Operation(summary = "사용자 쿠폰 통계", description = "사용자의 쿠폰 사용 통계를 조회합니다")
	@GetMapping("/users/{userId}/statistics")
	Mono<ResponseEntity<BaseResponse>> getUserCouponStatistics(
			@PathVariable Long userId,
			ServerHttpRequest httpRequest);

	@Operation(summary = "내 쿠폰 목록 조회 (레거시)", description = "사용자가 보유한 쿠폰 목록을 조회합니다 (페이지네이션)")
	@GetMapping("/users/me")
	Mono<ResponseEntity<BaseResponse>> getUserCouponsLegacy(
			@RequestParam(required = false) CouponStatus status,
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "20") Integer size,
			@RequestParam(defaultValue = "issuedAt,desc") String sort,
			ServerHttpRequest httpRequest);

	// ==================== 쿠폰 사용 ====================

	@Operation(summary = "쿠폰 예약", description = "주문 시 쿠폰을 예약합니다")
	@PostMapping("/reserve")
	Mono<ResponseEntity<BaseResponse>> reserveCoupon(
			@RequestBody CouponReserveRequest request,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 적용", description = "쿠폰 적용 가능 여부를 확인하고 적용합니다")
	@PostMapping("/apply")
	Mono<ResponseEntity<BaseResponse>> applyCoupon(
			@RequestBody CouponApplyRequest request,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 락 해제", description = "적용된 쿠폰의 락을 해제합니다")
	@DeleteMapping("/apply/{reservationId}")
	Mono<ResponseEntity<BaseResponse>> unlockCoupon(
			@PathVariable String reservationId,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 사용 확정", description = "결제 완료 후 쿠폰 사용을 확정합니다")
	@PostMapping("/use")
	Mono<ResponseEntity<BaseResponse>> useCoupon(
			@RequestBody CouponUseRequest request,
			ServerHttpRequest httpRequest);

	@Operation(summary = "쿠폰 예약 취소", description = "예약된 쿠폰을 취소합니다")
	@DeleteMapping("/reserve/{reservationId}")
	Mono<ResponseEntity<BaseResponse>> cancelReservation(
			@PathVariable String reservationId,
			ServerHttpRequest httpRequest);

	// ==================== 통계 ====================

	@Operation(summary = "실시간 쿠폰 통계", description = "특정 쿠폰 정책의 실시간 통계를 조회합니다")
	@GetMapping("/statistics/realtime/{policyId}")
	Mono<ResponseEntity<BaseResponse>> getRealtimeStatistics(
			@PathVariable Long policyId,
			ServerHttpRequest httpRequest);

	@Operation(summary = "전체 쿠폰 통계", description = "전체 쿠폰 시스템 통계를 조회합니다")
	@GetMapping("/statistics/global")
	Mono<ResponseEntity<BaseResponse>> getGlobalStatistics(ServerHttpRequest httpRequest);

	@Operation(summary = "사용자 쿠폰 통계", description = "특정 사용자의 쿠폰 사용 통계를 조회합니다")
	@GetMapping("/statistics/user/{userId}")
	Mono<ResponseEntity<BaseResponse>> getUserStatistics(
			@PathVariable Long userId,
			ServerHttpRequest httpRequest);

	@Operation(summary = "대시보드 통계", description = "대시보드용 쿠폰 통계 요약을 조회합니다")
	@GetMapping("/statistics/dashboard")
	Mono<ResponseEntity<BaseResponse>> getDashboardStatistics(ServerHttpRequest httpRequest);
}
