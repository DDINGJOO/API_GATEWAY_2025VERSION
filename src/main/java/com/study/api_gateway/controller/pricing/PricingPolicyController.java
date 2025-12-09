package com.study.api_gateway.controller.pricing;

import com.study.api_gateway.client.YeYakHaeYoClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.pricing.request.CopyPricingPolicyRequest;
import com.study.api_gateway.dto.pricing.request.DefaultPriceUpdateRequest;
import com.study.api_gateway.dto.pricing.request.TimeRangePricesUpdateRequest;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Pricing Policy", description = "가격 정책 관리 API")
public class PricingPolicyController {
	
	private final YeYakHaeYoClient yeYakHaeYoClient;
	private final ResponseFactory responseFactory;
	
	/**
	 * 룸별 가격 정책 조회
	 * GET /bff/v1/pricing-policies/{roomId}
	 */
	@GetMapping("/{roomId}")
	@Operation(summary = "가격 정책 조회", description = "특정 룸의 시간대별 가격 정책을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "PricingPolicySuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"roomId\": 101,\n    \"placeId\": 1,\n    \"timeSlot\": \"1시간\",\n    \"defaultPrice\": 15000,\n    \"timeRangePrices\": [\n      {\n        \"dayOfWeek\": \"MONDAY\",\n        \"startTime\": \"09:00\",\n        \"endTime\": \"18:00\",\n        \"price\": 15000\n      },\n      {\n        \"dayOfWeek\": \"FRIDAY\",\n        \"startTime\": \"18:00\",\n        \"endTime\": \"22:00\",\n        \"price\": 18000\n      },\n      {\n        \"dayOfWeek\": \"SATURDAY\",\n        \"startTime\": \"09:00\",\n        \"endTime\": \"22:00\",\n        \"price\": 20000\n      }\n    ]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/pricing-policies/101\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getPricingPolicy(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			ServerHttpRequest req
	) {
		log.info("가격 정책 조회: roomId={}", roomId);
		
		return yeYakHaeYoClient.getPricingPolicy(roomId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 특정 날짜의 시간대별 가격 조회
	 * GET /bff/v1/pricing-policies/{roomId}/date/{date}
	 */
	@GetMapping("/{roomId}/date/{date}")
	@Operation(summary = "날짜별 시간대 가격 조회", description = "특정 날짜의 시간대별 가격을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "TimeSlotPricesSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"roomId\": 101,\n    \"date\": \"2025-01-16\",\n    \"timeSlotPrices\": [\n      {\n        \"timeSlot\": \"09:00\",\n        \"price\": 15000\n      },\n      {\n        \"timeSlot\": \"10:00\",\n        \"price\": 15000\n      },\n      {\n        \"timeSlot\": \"18:00\",\n        \"price\": 18000\n      }\n    ]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/pricing-policies/101/date/2025-01-16\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getTimeSlotPrices(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@Parameter(description = "조회 날짜", required = true)
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			ServerHttpRequest req
	) {
		log.info("날짜별 시간대 가격 조회: roomId={}, date={}", roomId, date);
		
		return yeYakHaeYoClient.getTimeSlotPrices(roomId, date)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 기본 가격 업데이트
	 * PUT /bff/v1/pricing-policies/{roomId}/default-price
	 */
	@PutMapping("/{roomId}/default-price")
	@Operation(summary = "기본 가격 업데이트", description = "룸의 기본 가격을 업데이트합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공"),
			@ApiResponse(responseCode = "404", description = "가격 정책을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> updateDefaultPrice(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@RequestBody DefaultPriceUpdateRequest request,
			ServerHttpRequest req
	) {
		log.info("기본 가격 업데이트: roomId={}, newPrice={}", roomId, request.getDefaultPrice());
		
		return yeYakHaeYoClient.updateDefaultPrice(roomId, request)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 시간대별 가격 업데이트
	 * PUT /bff/v1/pricing-policies/{roomId}/time-range-prices
	 */
	@PutMapping("/{roomId}/time-range-prices")
	@Operation(summary = "시간대별 가격 업데이트", description = "특정 시간대의 가격을 업데이트합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공"),
			@ApiResponse(responseCode = "404", description = "가격 정책을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> updateTimeRangePrices(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@RequestBody TimeRangePricesUpdateRequest request,
			ServerHttpRequest req
	) {
		log.info("시간대별 가격 업데이트: roomId={}, timeRangePrices count={}",
				roomId, request.getTimeRangePrices().size());
		
		return yeYakHaeYoClient.updateTimeRangePrices(roomId, request)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 다른 룸의 가격 정책 복사
	 * POST /bff/v1/pricing-policies/{targetRoomId}/copy
	 */
	@PostMapping("/{targetRoomId}/copy")
	@Operation(summary = "가격 정책 복사", description = "다른 룸의 가격 정책을 복사합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "복사 성공"),
			@ApiResponse(responseCode = "404", description = "원본 가격 정책을 찾을 수 없음")
	})
	public Mono<ResponseEntity<BaseResponse>> copyPricingPolicy(
			@Parameter(description = "대상 룸 ID", required = true) @PathVariable Long targetRoomId,
			@RequestBody CopyPricingPolicyRequest request,
			ServerHttpRequest req
	) {
		log.info("가격 정책 복사: sourceRoomId={}, targetRoomId={}",
				request.getSourceRoomId(), targetRoomId);
		
		return yeYakHaeYoClient.copyPricingPolicy(targetRoomId, request)
				.map(response -> responseFactory.ok(response, req));
	}
}
