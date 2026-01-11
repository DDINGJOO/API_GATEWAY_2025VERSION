package com.study.api_gateway.api.pricing.controller;

import com.study.api_gateway.api.pricing.controller.PricingPolicyApi;
import com.study.api_gateway.api.reservation.service.ReservationFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.api.pricing.dto.request.CopyPricingPolicyRequest;
import com.study.api_gateway.api.pricing.dto.request.DefaultPriceUpdateRequest;
import com.study.api_gateway.api.pricing.dto.request.TimeRangePricesUpdateRequest;
import com.study.api_gateway.common.response.ResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * 클라이언트 앱용 가격 정책 관리 API
 * RESTful 방식의 가격 정책 CRUD 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/pricing-policies")
@RequiredArgsConstructor
public class PricingPolicyController implements PricingPolicyApi {

	private final ReservationFacadeService reservationFacadeService;
	private final ResponseFactory responseFactory;

	/**
	 * 룸별 가격 정책 조회
	 * GET /bff/v1/pricing-policies/{roomId}
	 */
	@Override
	@GetMapping("/{roomId}")
	public Mono<ResponseEntity<BaseResponse>> getPricingPolicy(
			@PathVariable Long roomId,
			ServerHttpRequest req
	) {
		log.info("가격 정책 조회: roomId={}", roomId);

		return reservationFacadeService.getPricingPolicy(roomId)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 특정 날짜의 시간대별 가격 조회
	 * GET /bff/v1/pricing-policies/{roomId}/date/{date}
	 */
	@Override
	@GetMapping("/{roomId}/date/{date}")
	public Mono<ResponseEntity<BaseResponse>> getTimeSlotPrices(
			@PathVariable Long roomId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			ServerHttpRequest req
	) {
		log.info("날짜별 시간대 가격 조회: roomId={}, date={}", roomId, date);

		return reservationFacadeService.getTimeSlotPrices(roomId, date)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 기본 가격 업데이트
	 * PUT /bff/v1/pricing-policies/{roomId}/default-price
	 */
	@Override
	@PutMapping("/{roomId}/default-price")
	public Mono<ResponseEntity<BaseResponse>> updateDefaultPrice(
			@PathVariable Long roomId,
			@RequestBody DefaultPriceUpdateRequest request,
			ServerHttpRequest req
	) {
		log.info("기본 가격 업데이트: roomId={}, newPrice={}", roomId, request.getDefaultPrice());

		return reservationFacadeService.updateDefaultPrice(roomId, request)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 시간대별 가격 업데이트
	 * PUT /bff/v1/pricing-policies/{roomId}/time-range-prices
	 */
	@Override
	@PutMapping("/{roomId}/time-range-prices")
	public Mono<ResponseEntity<BaseResponse>> updateTimeRangePrices(
			@PathVariable Long roomId,
			@RequestBody TimeRangePricesUpdateRequest request,
			ServerHttpRequest req
	) {
		log.info("시간대별 가격 업데이트: roomId={}, timeRangePrices count={}",
				roomId, request.getTimeRangePrices().size());

		return reservationFacadeService.updateTimeRangePrices(roomId, request)
				.map(response -> responseFactory.ok(response, req));
	}

	/**
	 * 다른 룸의 가격 정책 복사
	 * POST /bff/v1/pricing-policies/{targetRoomId}/copy
	 */
	@Override
	@PostMapping("/{targetRoomId}/copy")
	public Mono<ResponseEntity<BaseResponse>> copyPricingPolicy(
			@PathVariable Long targetRoomId,
			@RequestBody CopyPricingPolicyRequest request,
			ServerHttpRequest req
	) {
		log.info("가격 정책 복사: sourceRoomId={}, targetRoomId={}",
				request.getSourceRoomId(), targetRoomId);

		return reservationFacadeService.copyPricingPolicy(targetRoomId, request)
				.map(response -> responseFactory.ok(response, req));
	}
}
