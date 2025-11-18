package com.study.api_gateway.controller.product;

import com.study.api_gateway.client.YeYakHaeYoClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.product.enums.ProductScope;
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
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 클라이언트 앱용 상품 조회 API
 * RESTful 방식의 조회 전용 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 조회 API")
public class ProductController {
	
	private final YeYakHaeYoClient yeYakHaeYoClient;
	private final ResponseFactory responseFactory;
	
	/**
	 * 상품 ID로 조회
	 * GET /bff/v1/products/{productId}
	 */
	@GetMapping("/{productId}")
	@Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ProductDetailSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"productId\": 1,\n    \"scope\": \"ROOM\",\n    \"placeId\": 1,\n    \"roomId\": 101,\n    \"name\": \"1시간 이용권\",\n    \"pricingStrategy\": {\n      \"pricingType\": \"FIXED\",\n      \"initialPrice\": 15000,\n      \"additionalPrice\": 0\n    },\n    \"totalQuantity\": 10\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/products/1\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getProduct(
			@Parameter(description = "상품 ID", required = true) @PathVariable Long productId,
			ServerHttpRequest req
	) {
		log.info("상품 상세 조회: productId={}", productId);
		
		return yeYakHaeYoClient.getProductById(productId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 상품 목록 조회
	 * GET /bff/v1/products
	 */
	@GetMapping
	@Operation(summary = "상품 목록 조회", description = "다양한 조건으로 상품 목록을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ProductListSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"productId\": 1,\n      \"scope\": \"ROOM\",\n      \"placeId\": 1,\n      \"roomId\": 101,\n      \"name\": \"1시간 이용권\",\n      \"pricingStrategy\": {\n        \"pricingType\": \"FIXED\",\n        \"initialPrice\": 15000,\n        \"additionalPrice\": 0\n      },\n      \"totalQuantity\": 10\n    },\n    {\n      \"productId\": 2,\n      \"scope\": \"ROOM\",\n      \"placeId\": 1,\n      \"roomId\": 101,\n      \"name\": \"3시간 이용권\",\n      \"pricingStrategy\": {\n        \"pricingType\": \"HOURLY\",\n        \"initialPrice\": 15000,\n        \"additionalPrice\": 12000\n      },\n      \"totalQuantity\": 5\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/products?placeId=1&roomId=101\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getProducts(
			@Parameter(description = "상품 적용 범위") @RequestParam(required = false) ProductScope scope,
			@Parameter(description = "장소 ID") @RequestParam(required = false) Long placeId,
			@Parameter(description = "룸 ID") @RequestParam(required = false) Long roomId,
			ServerHttpRequest req
	) {
		log.info("상품 목록 조회: scope={}, placeId={}, roomId={}", scope, placeId, roomId);
		
		return yeYakHaeYoClient.getProducts(scope, placeId, roomId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 상품 재고 가용성 조회
	 * GET /bff/v1/products/availability
	 */
	@GetMapping("/availability")
	@Operation(summary = "상품 재고 가용성 조회", description = "특정 시간대에 예약 가능한 상품 목록과 수량을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ProductAvailabilitySuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"roomId\": 101,\n    \"placeId\": 1,\n    \"availableProducts\": [\n      {\n        \"productId\": 1,\n        \"productName\": \"1시간 이용권\",\n        \"unitPrice\": 15000,\n        \"availableQuantity\": 5,\n        \"totalStock\": 10\n      },\n      {\n        \"productId\": 2,\n        \"productName\": \"3시간 이용권\",\n        \"unitPrice\": 40000,\n        \"availableQuantity\": 3,\n        \"totalStock\": 5\n      }\n    ]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/products/availability?roomId=101&placeId=1&timeSlots=2025-01-16T14:00:00,2025-01-16T15:00:00\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getProductAvailability(
			@Parameter(description = "룸 ID", required = true) @RequestParam Long roomId,
			@Parameter(description = "장소 ID", required = true) @RequestParam Long placeId,
			@Parameter(description = "예약 시간 슬롯 목록", required = true) @RequestParam List<LocalDateTime> timeSlots,
			ServerHttpRequest req
	) {
		log.info("상품 재고 가용성 조회: roomId={}, placeId={}, timeSlots={}", roomId, placeId, timeSlots);
		
		return yeYakHaeYoClient.getProductAvailability(roomId, placeId, timeSlots)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 특정 룸에서 이용 가능한 상품 목록 조회
	 * GET /bff/v1/products/rooms/{roomId}/available
	 */
	@GetMapping("/rooms/{roomId}/available")
	@Operation(summary = "룸별 이용 가능 상품 조회", description = "특정 룸에서 이용 가능한 모든 상품을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "RoomProductsSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"productId\": 1,\n      \"scope\": \"ROOM\",\n      \"placeId\": 1,\n      \"roomId\": 101,\n      \"name\": \"1시간 이용권\",\n      \"pricingStrategy\": {\n        \"pricingType\": \"FIXED\",\n        \"initialPrice\": 15000,\n        \"additionalPrice\": 0\n      },\n      \"totalQuantity\": 10\n    },\n    {\n      \"productId\": 2,\n      \"scope\": \"ROOM\",\n      \"placeId\": 1,\n      \"roomId\": 101,\n      \"name\": \"3시간 이용권\",\n      \"pricingStrategy\": {\n        \"pricingType\": \"HOURLY\",\n        \"initialPrice\": 15000,\n        \"additionalPrice\": 12000\n      },\n      \"totalQuantity\": 5\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/products/rooms/101/available?placeId=1\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> getAvailableProductsForRoom(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			@Parameter(description = "장소 ID", required = true) @RequestParam Long placeId,
			ServerHttpRequest req
	) {
		log.info("룸별 이용 가능 상품 조회: roomId={}, placeId={}", roomId, placeId);
		
		return yeYakHaeYoClient.getAvailableProductsForRoom(roomId, placeId)
				.map(response -> responseFactory.ok(response, req));
	}
}
