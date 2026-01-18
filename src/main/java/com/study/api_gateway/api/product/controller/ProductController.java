package com.study.api_gateway.api.product.controller;

import com.study.api_gateway.api.product.dto.enums.ProductScope;
import com.study.api_gateway.api.product.dto.request.ProductCreateRequest;
import com.study.api_gateway.api.product.dto.request.ProductUpdateRequest;
import com.study.api_gateway.api.reservation.service.ReservationFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 클라이언트 앱용 상품 관리 API
 * RESTful 방식의 상품 CRUD 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductApi {
	
	private final ReservationFacadeService reservationFacadeService;
	private final ResponseFactory responseFactory;
	
	/**
	 * 상품 등록
	 * POST /bff/v1/products
	 */
	@Override
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> createProduct(
			@RequestBody ProductCreateRequest request,
			ServerHttpRequest req
	) {
		log.info("상품 등록: name={}, scope={}", request.getName(), request.getScope());
		
		return reservationFacadeService.createProduct(request)
				.map(response -> responseFactory.ok(response, req, org.springframework.http.HttpStatus.CREATED));
	}
	
	/**
	 * 상품 ID로 조회
	 * GET /bff/v1/products/{productId}
	 */
	@Override
	@GetMapping("/{productId}")
	public Mono<ResponseEntity<BaseResponse>> getProduct(
			@PathVariable Long productId,
			ServerHttpRequest req
	) {
		log.info("상품 상세 조회: productId={}", productId);
		
		return reservationFacadeService.getProductById(productId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 상품 목록 조회
	 * GET /bff/v1/products
	 */
	@Override
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getProducts(
			@RequestParam(required = false) ProductScope scope,
			@RequestParam(required = false) Long placeId,
			@RequestParam(required = false) Long roomId,
			ServerHttpRequest req
	) {
		log.info("상품 목록 조회: scope={}, placeId={}, roomId={}", scope, placeId, roomId);
		
		return reservationFacadeService.getProducts(scope, placeId, roomId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 상품 재고 가용성 조회
	 * GET /bff/v1/products/availability
	 */
	@Override
	@GetMapping("/availability")
	public Mono<ResponseEntity<BaseResponse>> getProductAvailability(
			@RequestParam Long roomId,
			@RequestParam Long placeId,
			@RequestParam List<LocalDateTime> timeSlots,
			ServerHttpRequest req
	) {
		log.info("상품 재고 가용성 조회: roomId={}, placeId={}, timeSlots={}", roomId, placeId, timeSlots);
		
		return reservationFacadeService.getProductAvailability(roomId, placeId, timeSlots)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 특정 룸에서 이용 가능한 상품 목록 조회
	 * GET /bff/v1/products/rooms/{roomId}/available
	 */
	@Override
	@GetMapping("/rooms/{roomId}/available")
	public Mono<ResponseEntity<BaseResponse>> getAvailableProductsForRoom(
			@PathVariable Long roomId,
			@RequestParam Long placeId,
			ServerHttpRequest req
	) {
		log.info("룸별 이용 가능 상품 조회: roomId={}, placeId={}", roomId, placeId);
		
		return reservationFacadeService.getAvailableProductsForRoom(roomId, placeId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 상품 수정
	 * PUT /bff/v1/products/{productId}
	 */
	@Override
	@PutMapping("/{productId}")
	public Mono<ResponseEntity<BaseResponse>> updateProduct(
			@PathVariable Long productId,
			@RequestBody ProductUpdateRequest request,
			ServerHttpRequest req
	) {
		log.info("상품 수정: productId={}", productId);
		
		return reservationFacadeService.updateProduct(productId, request)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 상품 삭제
	 * DELETE /bff/v1/products/{productId}
	 */
	@Override
	@DeleteMapping("/{productId}")
	public Mono<ResponseEntity<Void>> deleteProduct(
			@PathVariable Long productId
	) {
		log.info("상품 삭제: productId={}", productId);
		
		return reservationFacadeService.deleteProduct(productId)
				.map(response -> ResponseEntity.noContent().build());
	}
}
