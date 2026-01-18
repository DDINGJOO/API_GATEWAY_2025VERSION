package com.study.api_gateway.api.product.controller;

import com.study.api_gateway.api.product.dto.enums.ProductScope;
import com.study.api_gateway.api.product.dto.request.ProductCreateRequest;
import com.study.api_gateway.api.product.dto.request.ProductUpdateRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Product", description = "상품 관리 API")
public interface ProductApi {
	
	@Operation(summary = "상품 등록", description = "새로운 추가상품을 등록합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "등록 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> createProduct(
			@RequestBody ProductCreateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/{productId}")
	Mono<ResponseEntity<BaseResponse>> getProduct(
			@Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
			ServerHttpRequest req);
	
	@Operation(summary = "상품 목록 조회", description = "다양한 조건으로 상품 목록을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping
	Mono<ResponseEntity<BaseResponse>> getProducts(
			@Parameter(description = "상품 적용 범위") @RequestParam(required = false) ProductScope scope,
			@Parameter(description = "장소 ID") @RequestParam(required = false) Long placeId,
			@Parameter(description = "룸 ID") @RequestParam(required = false) Long roomId,
			ServerHttpRequest req);
	
	@Operation(summary = "상품 재고 가용성 조회", description = "특정 시간대에 예약 가능한 상품 목록과 수량을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/availability")
	Mono<ResponseEntity<BaseResponse>> getProductAvailability(
			@Parameter(description = "룸 ID", required = true) @RequestParam Long roomId,
			@Parameter(description = "장소 ID", required = true) @RequestParam Long placeId,
			@Parameter(description = "예약 시간 슬롯 목록", required = true) @RequestParam List<LocalDateTime> timeSlots,
			ServerHttpRequest req);
	
	@Operation(summary = "룸별 이용 가능 상품 조회", description = "특정 룸에서 이용 가능한 모든 상품을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/rooms/{roomId}/available")
	Mono<ResponseEntity<BaseResponse>> getAvailableProductsForRoom(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@Parameter(description = "장소 ID", required = true) @RequestParam Long placeId,
			ServerHttpRequest req);
	
	@Operation(summary = "상품 수정", description = "상품 정보를 수정합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
	})
	@PutMapping("/{productId}")
	Mono<ResponseEntity<BaseResponse>> updateProduct(
			@Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
			@RequestBody ProductUpdateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "상품 삭제", description = "상품을 삭제합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제 성공"),
			@ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
	})
	@DeleteMapping("/{productId}")
	Mono<ResponseEntity<Void>> deleteProduct(
			@Parameter(description = "상품 ID", required = true) @PathVariable Long productId);
}
