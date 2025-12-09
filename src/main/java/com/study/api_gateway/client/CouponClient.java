package com.study.api_gateway.client;

import com.study.api_gateway.dto.coupon.enums.CouponStatus;
import com.study.api_gateway.dto.coupon.request.*;
import com.study.api_gateway.dto.coupon.response.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 쿠폰 서비스와 통신하는 WebClient 기반 클라이언트
 */
@Component
public class CouponClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/coupons";
	private final String POLICY_PREFIX = "/api/coupon-policies";
	
	public CouponClient(@Qualifier("couponWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 쿠폰 정책 생성 API
	 * POST /api/coupons/policies
	 */
	public Mono<CouponPolicyResponse> createCouponPolicy(CouponPolicyCreateRequest request) {
		return webClient.post()
				.uri(PREFIX + "/policies")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(CouponPolicyResponse.class);
	}
	
	/**
	 * 쿠폰 정책 조회 API
	 * GET /api/coupons/policies/{policyId}
	 */
	public Mono<CouponPolicyResponse> getCouponPolicy(Long policyId) {
		return webClient.get()
				.uri(PREFIX + "/policies/{policyId}", policyId)
				.retrieve()
				.bodyToMono(CouponPolicyResponse.class);
	}
	
	/**
	 * 쿠폰 정책 수정 API
	 * PATCH /api/coupons/policies/{policyId}
	 */
	public Mono<CouponPolicyResponse> updateCouponPolicy(Long policyId, CouponPolicyCreateRequest request) {
		return webClient.patch()
				.uri(PREFIX + "/policies/{policyId}", policyId)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(CouponPolicyResponse.class);
	}
	
	/**
	 * 쿠폰 정책 비활성화 API
	 * DELETE /api/coupons/policies/{policyId}
	 */
	public Mono<Void> deleteCouponPolicy(Long policyId) {
		return webClient.delete()
				.uri(PREFIX + "/policies/{policyId}", policyId)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	/**
	 * 쿠폰 정책 남은 발급 수량 수정 API
	 * PATCH /api/coupon-policies/{policyId}/remaining-quantity
	 */
	public Mono<PolicyQuantityUpdateResponse> updatePolicyQuantity(Long policyId, PolicyQuantityUpdateRequest request) {
		return webClient.patch()
				.uri(POLICY_PREFIX + "/{policyId}/remaining-quantity", policyId)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(PolicyQuantityUpdateResponse.class);
	}
	
	/**
	 * 쿠폰 다운로드 (코드로 발급) API
	 * POST /api/coupons/download
	 */
	public Mono<CouponIssueResponse> downloadCoupon(CouponDownloadRequest request) {
		return webClient.post()
				.uri(PREFIX + "/download")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(CouponIssueResponse.class);
	}
	
	/**
	 * 쿠폰 코드 유효성 확인 API
	 * GET /api/coupons/validate/{couponCode}
	 */
	public Mono<CouponValidateResponse> validateCouponCode(String couponCode) {
		return webClient.get()
				.uri(PREFIX + "/validate/{couponCode}", couponCode)
				.retrieve()
				.bodyToMono(CouponValidateResponse.class);
	}
	
	/**
	 * 직접 발급 API
	 * POST /api/coupons/direct-issue
	 */
	public Mono<CouponDirectIssueResponse> issueDirectCoupon(CouponDirectIssueRequest request) {
		return webClient.post()
				.uri(PREFIX + "/direct-issue")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(CouponDirectIssueResponse.class);
	}
	
	/**
	 * 선착순 발급 API
	 * POST /api/coupons/issue/fcfs
	 */
	public Mono<CouponIssueResponse> issueFcfsCoupon(Long policyId, Long userId) {
		return webClient.post()
				.uri(PREFIX + "/issue/fcfs")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new FcfsIssueRequest(policyId, userId))
				.retrieve()
				.bodyToMono(CouponIssueResponse.class);
	}
	
	/**
	 * 사용자 쿠폰 목록 조회 (커서 기반) API
	 * GET /api/coupons/users/{userId}
	 */
	public Mono<UserCouponCursorResponse> getUserCoupons(
			Long userId,
			CouponStatus status,
			List<Long> productIds,
			Long cursor,
			Integer limit
	) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/users/{userId}");
					
					if (status != null) uriBuilder.queryParam("status", status);
					if (productIds != null && !productIds.isEmpty()) {
						productIds.forEach(id -> uriBuilder.queryParam("productIds", id));
					}
					if (cursor != null) uriBuilder.queryParam("cursor", cursor);
					if (limit != null) uriBuilder.queryParam("limit", limit);
					
					return uriBuilder.build(userId);
				})
				.retrieve()
				.bodyToMono(UserCouponCursorResponse.class);
	}
	
	/**
	 * 만료 임박 쿠폰 조회 API
	 * GET /api/coupons/users/{userId}/expiring
	 */
	public Mono<ExpiringCouponsResponse> getExpiringCoupons(Long userId, Integer days, Integer limit) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/users/{userId}/expiring");
					
					if (days != null) uriBuilder.queryParam("days", days);
					if (limit != null) uriBuilder.queryParam("limit", limit);
					
					return uriBuilder.build(userId);
				})
				.retrieve()
				.bodyToMono(ExpiringCouponsResponse.class);
	}
	
	/**
	 * 사용자 쿠폰 통계 API
	 * GET /api/coupons/users/{userId}/statistics
	 */
	public Mono<UserCouponStatisticsResponse> getUserCouponStatistics(Long userId) {
		return webClient.get()
				.uri(PREFIX + "/users/{userId}/statistics", userId)
				.retrieve()
				.bodyToMono(UserCouponStatisticsResponse.class);
	}
	
	/**
	 * 사용자 쿠폰 목록 조회 (레거시) API
	 * GET /api/coupons/users/me
	 */
	public Mono<UserCouponListResponse> getUserCouponsLegacy(
			Long userId,
			CouponStatus status,
			Integer page,
			Integer size,
			String sort
	) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(PREFIX + "/users/me");
					
					if (status != null) uriBuilder.queryParam("status", status);
					if (page != null) uriBuilder.queryParam("page", page);
					if (size != null) uriBuilder.queryParam("size", size);
					if (sort != null) uriBuilder.queryParam("sort", sort);
					
					return uriBuilder.build();
				})
				.header("X-User-Id", String.valueOf(userId))
				.retrieve()
				.bodyToMono(UserCouponListResponse.class);
	}
	
	/**
	 * 쿠폰 예약 API
	 * POST /api/coupons/reserve
	 */
	public Mono<CouponReservationResponse> reserveCoupon(CouponReserveRequest request) {
		return webClient.post()
				.uri(PREFIX + "/reserve")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(CouponReservationResponse.class);
	}
	
	/**
	 * 쿠폰 적용 API
	 * POST /api/coupons/apply
	 */
	public Mono<CouponApplyResponse> applyCoupon(CouponApplyRequest request) {
		return webClient.post()
				.uri(PREFIX + "/apply")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(CouponApplyResponse.class);
	}
	
	/**
	 * 쿠폰 락 해제 API
	 * DELETE /api/coupons/apply/{reservationId}
	 */
	public Mono<Void> unlockCoupon(String reservationId) {
		return webClient.delete()
				.uri(PREFIX + "/apply/{reservationId}", reservationId)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	/**
	 * 쿠폰 사용 확정 API
	 * POST /api/coupons/use
	 */
	public Mono<CouponUseResponse> useCoupon(CouponUseRequest request) {
		return webClient.post()
				.uri(PREFIX + "/use")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(CouponUseResponse.class);
	}
	
	/**
	 * 쿠폰 예약 취소 API
	 * DELETE /api/coupons/reserve/{reservationId}
	 */
	public Mono<CouponReservationResponse> cancelReservation(String reservationId) {
		return webClient.delete()
				.uri(PREFIX + "/reserve/{reservationId}", reservationId)
				.retrieve()
				.bodyToMono(CouponReservationResponse.class);
	}
	
	/**
	 * 실시간 통계 API
	 * GET /api/coupons/statistics/realtime/{policyId}
	 */
	public Mono<Object> getRealtimeStatistics(Long policyId) {
		return webClient.get()
				.uri(PREFIX + "/statistics/realtime/{policyId}", policyId)
				.retrieve()
				.bodyToMono(Object.class);
	}
	
	/**
	 * 전체 통계 API
	 * GET /api/coupons/statistics/global
	 */
	public Mono<Object> getGlobalStatistics() {
		return webClient.get()
				.uri(PREFIX + "/statistics/global")
				.retrieve()
				.bodyToMono(Object.class);
	}
	
	/**
	 * 사용자 통계 API
	 * GET /api/coupons/statistics/user/{userId}
	 */
	public Mono<Object> getUserStatistics(Long userId) {
		return webClient.get()
				.uri(PREFIX + "/statistics/user/{userId}", userId)
				.retrieve()
				.bodyToMono(Object.class);
	}
	
	/**
	 * 대시보드 요약 API
	 * GET /api/coupons/statistics/dashboard
	 */
	public Mono<Object> getDashboardStatistics() {
		return webClient.get()
				.uri(PREFIX + "/statistics/dashboard")
				.retrieve()
				.bodyToMono(Object.class);
	}
	
	/**
	 * 선착순 발급을 위한 내부 request 클래스
	 */
	private static class FcfsIssueRequest {
		private Long policyId;
		private Long userId;
		
		public FcfsIssueRequest(Long policyId, Long userId) {
			this.policyId = policyId;
			this.userId = userId;
		}
		
		public Long getPolicyId() {
			return policyId;
		}
		
		public Long getUserId() {
			return userId;
		}
	}
}
