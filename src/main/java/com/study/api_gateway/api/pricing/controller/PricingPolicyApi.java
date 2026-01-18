package com.study.api_gateway.api.pricing.controller;

import com.study.api_gateway.api.pricing.dto.request.CopyPricingPolicyRequest;
import com.study.api_gateway.api.pricing.dto.request.DefaultPriceUpdateRequest;
import com.study.api_gateway.api.pricing.dto.request.TimeRangePricesUpdateRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * 가격 정책 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Pricing Policy", description = "가격 정책 관리 API")
public interface PricingPolicyApi {
	
	@Operation(summary = "가격 정책 조회", description = "특정 룸의 시간대별 가격 정책을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/{roomId}")
	Mono<ResponseEntity<BaseResponse>> getPricingPolicy(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			ServerHttpRequest req);
	
	@Operation(summary = "날짜별 시간대 가격 조회", description = "특정 날짜의 시간대별 가격을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/{roomId}/date/{date}")
	Mono<ResponseEntity<BaseResponse>> getTimeSlotPrices(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@Parameter(description = "조회 날짜", required = true)
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			ServerHttpRequest req);
	
	@Operation(summary = "기본 가격 업데이트", description = "룸의 기본 가격을 업데이트합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공"),
			@ApiResponse(responseCode = "404", description = "가격 정책을 찾을 수 없음")
	})
	@PutMapping("/{roomId}/default-price")
	Mono<ResponseEntity<BaseResponse>> updateDefaultPrice(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@RequestBody DefaultPriceUpdateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "시간대별 가격 업데이트", description = "특정 시간대의 가격을 업데이트합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공"),
			@ApiResponse(responseCode = "404", description = "가격 정책을 찾을 수 없음")
	})
	@PutMapping("/{roomId}/time-range-prices")
	Mono<ResponseEntity<BaseResponse>> updateTimeRangePrices(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@RequestBody TimeRangePricesUpdateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "가격 정책 복사", description = "다른 룸의 가격 정책을 복사합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "복사 성공"),
			@ApiResponse(responseCode = "404", description = "원본 가격 정책을 찾을 수 없음")
	})
	@PostMapping("/{targetRoomId}/copy")
	Mono<ResponseEntity<BaseResponse>> copyPricingPolicy(
			@Parameter(description = "대상 룸 ID", required = true) @PathVariable Long targetRoomId,
			@RequestBody CopyPricingPolicyRequest request,
			ServerHttpRequest req);
}
