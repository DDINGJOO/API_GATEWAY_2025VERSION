package com.study.api_gateway.controller.coupon;

import com.study.api_gateway.client.CouponClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 쿠폰 관리자용 API Gateway 컨트롤러
 */
@RestController
@RequestMapping("/bff/v1/admin/coupons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coupon Admin", description = "쿠폰 관리자 API")
public class CouponAdminController {
	private final CouponClient couponClient;
	private final ResponseFactory responseFactory;
	
	/**
	 * 재고 동기화
	 */
	@Operation(summary = "재고 동기화", description = "DB와 Redis 간 쿠폰 재고를 동기화합니다")
	@PostMapping("/stock/sync")
	public Mono<ResponseEntity<BaseResponse>> syncStock(
			@RequestBody Map<String, Object> request,
			ServerHttpRequest httpRequest) {
		
		Long policyId = Long.valueOf(request.get("policyId").toString());
		Boolean forceSync = (Boolean) request.getOrDefault("forceSync", false);
		
		log.info("[재고 동기화] policyId: {}, forceSync: {}", policyId, forceSync);
		
		// 이 부분은 실제 쿠폰 서비스에 맞게 구현되어야 합니다
		return Mono.just(responseFactory.ok(
				Map.of("message", "Stock sync endpoint - to be implemented"),
				httpRequest
		));
	}
	
	/**
	 * 만료 쿠폰 처리
	 */
	@Operation(summary = "만료 쿠폰 처리", description = "만료된 쿠폰들을 일괄 처리합니다")
	@PostMapping("/expire")
	public Mono<ResponseEntity<BaseResponse>> processExpiredCoupons(
			@RequestBody Map<String, Object> request,
			ServerHttpRequest httpRequest) {
		
		Integer batchSize = (Integer) request.getOrDefault("batchSize", 1000);
		Boolean dryRun = (Boolean) request.getOrDefault("dryRun", false);
		
		log.info("[만료 쿠폰 처리] batchSize: {}, dryRun: {}", batchSize, dryRun);
		
		// 이 부분은 실제 쿠폰 서비스에 맞게 구현되어야 합니다
		return Mono.just(responseFactory.ok(
				Map.of("message", "Expire process endpoint - to be implemented"),
				httpRequest
		));
	}
	
	/**
	 * 예약 타임아웃 처리
	 */
	@Operation(summary = "예약 타임아웃 처리", description = "타임아웃된 쿠폰 예약을 일괄 처리합니다")
	@PostMapping("/reservations/timeout")
	public Mono<ResponseEntity<BaseResponse>> processTimeoutReservations(
			@RequestBody Map<String, Object> request,
			ServerHttpRequest httpRequest) {
		
		Integer timeoutMinutes = (Integer) request.getOrDefault("timeoutMinutes", 30);
		Integer batchSize = (Integer) request.getOrDefault("batchSize", 100);
		
		log.info("[예약 타임아웃 처리] timeoutMinutes: {}, batchSize: {}", timeoutMinutes, batchSize);
		
		// 이 부분은 실제 쿠폰 서비스에 맞게 구현되어야 합니다
		return Mono.just(responseFactory.ok(
				Map.of("message", "Timeout process endpoint - to be implemented"),
				httpRequest
		));
	}
	
	/**
	 * 쿠폰 정책 재고 조정
	 */
	@Operation(summary = "쿠폰 재고 조정", description = "쿠폰 정책의 재고를 수동으로 조정합니다")
	@PatchMapping("/policies/{policyId}/stock")
	public Mono<ResponseEntity<BaseResponse>> adjustStock(
			@PathVariable Long policyId,
			@RequestBody Map<String, Object> request,
			ServerHttpRequest httpRequest) {
		
		Integer adjustment = Integer.valueOf(request.get("adjustment").toString());
		String reason = (String) request.get("reason");
		
		log.info("[재고 조정] policyId: {}, adjustment: {}, reason: {}",
				policyId, adjustment, reason);
		
		// 이 부분은 실제 쿠폰 서비스에 맞게 구현되어야 합니다
		return Mono.just(responseFactory.ok(
				Map.of("message", "Stock adjustment endpoint - to be implemented"),
				httpRequest
		));
	}
	
	/**
	 * 배치 발급
	 */
	@Operation(summary = "배치 쿠폰 발급", description = "대량의 쿠폰을 배치로 발급합니다")
	@PostMapping("/issue/batch")
	public Mono<ResponseEntity<BaseResponse>> batchIssueCoupons(
			@RequestBody Map<String, Object> request,
			ServerHttpRequest httpRequest) {
		
		log.info("[배치 발급] request: {}", request);
		
		// 이 부분은 실제 쿠폰 서비스에 맞게 구현되어야 합니다
		return Mono.just(responseFactory.ok(
				Map.of("message", "Batch issue endpoint - to be implemented"),
				httpRequest
		));
	}
}
