package com.study.api_gateway.api.coupon.service;

import com.study.api_gateway.api.coupon.client.CouponClient;
import com.study.api_gateway.api.coupon.dto.enums.CouponStatus;
import com.study.api_gateway.api.coupon.dto.request.*;
import com.study.api_gateway.api.coupon.dto.response.*;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Coupon 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class CouponFacadeService {
	
	private static final String SERVICE_NAME = "coupon-service";
	private final CouponClient couponClient;
	private final ResilienceOperator resilience;
	
	// ==================== 쿠폰 정책 API ====================
	
	public Mono<CouponPolicyResponse> createCouponPolicy(CouponPolicyCreateRequest request) {
		return couponClient.createCouponPolicy(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponPolicyResponse> getCouponPolicy(Long policyId) {
		return couponClient.getCouponPolicy(policyId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponPolicyResponse> updateCouponPolicy(Long policyId, CouponPolicyCreateRequest request) {
		return couponClient.updateCouponPolicy(policyId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> deleteCouponPolicy(Long policyId) {
		return couponClient.deleteCouponPolicy(policyId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<PolicyQuantityUpdateResponse> updatePolicyQuantity(Long policyId, PolicyQuantityUpdateRequest request) {
		return couponClient.updatePolicyQuantity(policyId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 쿠폰 발급 API ====================
	
	public Mono<CouponIssueResponse> downloadCoupon(CouponDownloadRequest request) {
		return couponClient.downloadCoupon(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponValidateResponse> validateCouponCode(String couponCode) {
		return couponClient.validateCouponCode(couponCode)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponDirectIssueResponse> issueDirectCoupon(CouponDirectIssueRequest request) {
		return couponClient.issueDirectCoupon(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponIssueResponse> issueFcfsCoupon(Long policyId, Long userId) {
		return couponClient.issueFcfsCoupon(policyId, userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 사용자 쿠폰 API ====================
	
	public Mono<UserCouponCursorResponse> getUserCoupons(
			Long userId,
			CouponStatus status,
			List<Long> productIds,
			Long cursor,
			Integer limit
	) {
		return couponClient.getUserCoupons(userId, status, productIds, cursor, limit)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ExpiringCouponsResponse> getExpiringCoupons(Long userId, Integer days, Integer limit) {
		return couponClient.getExpiringCoupons(userId, days, limit)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<UserCouponStatisticsResponse> getUserCouponStatistics(Long userId) {
		return couponClient.getUserCouponStatistics(userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<UserCouponListResponse> getUserCouponsLegacy(
			Long userId,
			CouponStatus status,
			Integer page,
			Integer size,
			String sort
	) {
		return couponClient.getUserCouponsLegacy(userId, status, page, size, sort)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 쿠폰 사용 API ====================
	
	public Mono<CouponReservationResponse> reserveCoupon(CouponReserveRequest request) {
		return couponClient.reserveCoupon(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponApplyResponse> applyCoupon(CouponApplyRequest request) {
		return couponClient.applyCoupon(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> unlockCoupon(String reservationId) {
		return couponClient.unlockCoupon(reservationId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponUseResponse> useCoupon(CouponUseRequest request) {
		return couponClient.useCoupon(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<CouponReservationResponse> cancelReservation(String reservationId) {
		return couponClient.cancelReservation(reservationId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ==================== 통계 API ====================
	
	public Mono<Object> getRealtimeStatistics(Long policyId) {
		return couponClient.getRealtimeStatistics(policyId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Object> getGlobalStatistics() {
		return couponClient.getGlobalStatistics()
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Object> getUserStatistics(Long userId) {
		return couponClient.getUserStatistics(userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Object> getDashboardStatistics() {
		return couponClient.getDashboardStatistics()
				.transform(resilience.protect(SERVICE_NAME));
	}
}
