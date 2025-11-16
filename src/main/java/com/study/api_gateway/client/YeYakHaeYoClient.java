package com.study.api_gateway.client;

import com.study.api_gateway.dto.pricing.response.PricingPolicyResponse;
import com.study.api_gateway.dto.product.enums.ProductScope;
import com.study.api_gateway.dto.product.response.ProductAvailabilityResponse;
import com.study.api_gateway.dto.product.response.ProductResponse;
import com.study.api_gateway.dto.reservation.response.PricePreviewResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * YeYakHaeYo Server와 통신하는 WebClient 기반 클라이언트
 * 조회 전용 API 제공 (가격 정책, 상품, 예약 가격 미리보기)
 */
@Component
public class YeYakHaeYoClient {
	private final WebClient webClient;
	
	public YeYakHaeYoClient(@Qualifier("yeYakHaeYoWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 가격 정책 조회
	 * GET /api/v1/pricing-policies/{roomId}
	 */
	public Mono<PricingPolicyResponse> getPricingPolicy(Long roomId) {
		String uriString = "/api/v1/pricing-policies/" + roomId;
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(PricingPolicyResponse.class);
	}
	
	/**
	 * 상품 ID로 조회
	 * GET /api/v1/products/{productId}
	 */
	public Mono<ProductResponse> getProductById(Long productId) {
		String uriString = "/api/v1/products/" + productId;
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ProductResponse.class);
	}
	
	/**
	 * 상품 목록 조회
	 * GET /api/v1/products
	 */
	public Mono<List<ProductResponse>> getProducts(
			ProductScope scope,
			Long placeId,
			Long roomId
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/v1/products");
		
		if (scope != null) builder.queryParam("scope", scope);
		if (placeId != null) builder.queryParam("placeId", placeId);
		if (roomId != null) builder.queryParam("roomId", roomId);
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToFlux(ProductResponse.class)
				.collectList();
	}
	
	/**
	 * 상품 재고 가용성 조회
	 * GET /api/v1/products/availability
	 */
	public Mono<ProductAvailabilityResponse> getProductAvailability(
			Long roomId,
			Long placeId,
			List<LocalDateTime> timeSlots
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/v1/products/availability");
		
		builder.queryParam("roomId", roomId);
		builder.queryParam("placeId", placeId);
		if (timeSlots != null && !timeSlots.isEmpty()) {
			timeSlots.forEach(slot -> builder.queryParam("timeSlots", slot.toString()));
		}
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ProductAvailabilityResponse.class);
	}
	
	/**
	 * 특정 룸에서 이용 가능한 상품 목록 조회
	 * GET /api/v1/rooms/{roomId}/available-products
	 */
	public Mono<List<ProductResponse>> getAvailableProductsForRoom(
			Long roomId,
			Long placeId
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(
				"/api/v1/rooms/" + roomId + "/available-products");
		
		builder.queryParam("placeId", placeId);
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToFlux(ProductResponse.class)
				.collectList();
	}
	
	/**
	 * 예약 가격 미리보기
	 * POST /api/v1/reservations/preview
	 * <p>
	 * Note: 조회용 API이지만 POST 메서드 사용
	 */
	public Mono<PricePreviewResponse> previewPrice(Object request) {
		String uriString = "/api/v1/reservations/preview";
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(PricePreviewResponse.class);
	}
}
