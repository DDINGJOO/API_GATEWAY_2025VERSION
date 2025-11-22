package com.study.api_gateway.client;

import com.study.api_gateway.dto.pricing.request.CopyPricingPolicyRequest;
import com.study.api_gateway.dto.pricing.request.DefaultPriceUpdateRequest;
import com.study.api_gateway.dto.pricing.request.TimeRangePricesUpdateRequest;
import com.study.api_gateway.dto.pricing.response.PricingPolicyResponse;
import com.study.api_gateway.dto.pricing.response.TimeSlotPricesResponse;
import com.study.api_gateway.dto.product.enums.ProductScope;
import com.study.api_gateway.dto.product.request.ProductCreateRequest;
import com.study.api_gateway.dto.product.request.ProductUpdateRequest;
import com.study.api_gateway.dto.product.request.RoomAllowedProductsRequest;
import com.study.api_gateway.dto.product.response.ProductAvailabilityResponse;
import com.study.api_gateway.dto.product.response.ProductResponse;
import com.study.api_gateway.dto.product.response.RoomAllowedProductsResponse;
import com.study.api_gateway.dto.reservation.request.ReservationPreviewRequest;
import com.study.api_gateway.dto.reservation.request.UpdateReservationProductsRequest;
import com.study.api_gateway.dto.reservation.response.ReservationPreviewResponse;
import com.study.api_gateway.dto.reservation.response.ReservationPriceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * YeYakHaeYo Server와 통신하는 WebClient 기반 클라이언트
 * 상품, 가격 정책, 예약 가격 관리 API 제공
 */
@Component
public class YeYakHaeYoClient {
	private final WebClient webClient;

	public YeYakHaeYoClient(@Qualifier("yeYakHaeYoWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	// ========== 상품 관리 API ==========
	
	/**
	 * 상품 등록
	 * POST /api/v1/products
	 */
	public Mono<ProductResponse> createProduct(ProductCreateRequest request) {
		return webClient.post()
				.uri("/api/v1/products")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ProductResponse.class);
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
	 * 상품 수정
	 * PUT /api/v1/products/{productId}
	 */
	public Mono<ProductResponse> updateProduct(Long productId, ProductUpdateRequest request) {
		return webClient.put()
				.uri("/api/v1/products/" + productId)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ProductResponse.class);
	}
	
	/**
	 * 상품 삭제
	 * DELETE /api/v1/products/{productId}
	 */
	public Mono<Void> deleteProduct(Long productId) {
		return webClient.delete()
				.uri("/api/v1/products/" + productId)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	// ========== 가격 정책 관리 API ==========
	
	/**
	 * 특정 날짜의 시간대별 가격 조회
	 * GET /api/v1/pricing-policies/{roomId}/date/{date}
	 */
	public Mono<TimeSlotPricesResponse> getTimeSlotPrices(Long roomId, LocalDate date) {
		return webClient.get()
				.uri("/api/v1/pricing-policies/" + roomId + "/date/" + date.toString())
				.retrieve()
				.bodyToMono(TimeSlotPricesResponse.class);
	}
	
	/**
	 * 기본 가격 업데이트
	 * PUT /api/v1/pricing-policies/{roomId}/default-price
	 */
	public Mono<PricingPolicyResponse> updateDefaultPrice(Long roomId, DefaultPriceUpdateRequest request) {
		return webClient.put()
				.uri("/api/v1/pricing-policies/" + roomId + "/default-price")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(PricingPolicyResponse.class);
	}
	
	/**
	 * 시간대별 가격 업데이트
	 * PUT /api/v1/pricing-policies/{roomId}/time-range-prices
	 */
	public Mono<PricingPolicyResponse> updateTimeRangePrices(Long roomId, TimeRangePricesUpdateRequest request) {
		return webClient.put()
				.uri("/api/v1/pricing-policies/" + roomId + "/time-range-prices")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(PricingPolicyResponse.class);
	}
	
	/**
	 * 다른 룸의 가격 정책 복사
	 * POST /api/v1/pricing-policies/{targetRoomId}/copy
	 */
	public Mono<PricingPolicyResponse> copyPricingPolicy(Long targetRoomId, CopyPricingPolicyRequest request) {
		return webClient.post()
				.uri("/api/v1/pricing-policies/" + targetRoomId + "/copy")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(PricingPolicyResponse.class);
	}
	
	// ========== 예약 가격 관리 API ==========
	
	/**
	 * 예약 가격 미리보기
	 * POST /api/v1/reservations/preview
	 */
	public Mono<ReservationPreviewResponse> previewReservation(ReservationPreviewRequest request) {
		return webClient.post()
				.uri("/api/v1/reservations/preview")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ReservationPreviewResponse.class);
	}
	
	/**
	 * 예약 확정
	 * PUT /api/v1/reservations/{reservationId}/confirm
	 */
	public Mono<ReservationPriceResponse> confirmReservation(Long reservationId) {
		return webClient.put()
				.uri("/api/v1/reservations/" + reservationId + "/confirm")
				.retrieve()
				.bodyToMono(ReservationPriceResponse.class);
	}
	
	/**
	 * 예약 취소
	 * PUT /api/v1/reservations/{reservationId}/cancel
	 */
	public Mono<ReservationPriceResponse> cancelReservation(Long reservationId) {
		return webClient.put()
				.uri("/api/v1/reservations/" + reservationId + "/cancel")
				.retrieve()
				.bodyToMono(ReservationPriceResponse.class);
	}
	
	/**
	 * 예약 상품 업데이트
	 * PUT /api/v1/reservations/{reservationId}/products
	 */
	public Mono<ReservationPriceResponse> updateReservationProducts(
			Long reservationId,
			UpdateReservationProductsRequest request
	) {
		return webClient.put()
				.uri("/api/v1/reservations/" + reservationId + "/products")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ReservationPriceResponse.class);
	}
	
	// ========== 룸 허용 상품 관리 API (Admin) ==========
	
	/**
	 * 룸 허용 상품 설정
	 * POST /api/v1/admin/rooms/{roomId}/allowed-products
	 */
	public Mono<RoomAllowedProductsResponse> setRoomAllowedProducts(
			Long roomId,
			RoomAllowedProductsRequest request
	) {
		return webClient.post()
				.uri("/api/v1/admin/rooms/" + roomId + "/allowed-products")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(RoomAllowedProductsResponse.class);
	}
	
	/**
	 * 룸 허용 상품 조회
	 * GET /api/v1/admin/rooms/{roomId}/allowed-products
	 */
	public Mono<RoomAllowedProductsResponse> getRoomAllowedProducts(Long roomId) {
		return webClient.get()
				.uri("/api/v1/admin/rooms/" + roomId + "/allowed-products")
				.retrieve()
				.bodyToMono(RoomAllowedProductsResponse.class);
	}
	
	/**
	 * 룸 허용 상품 삭제
	 * DELETE /api/v1/admin/rooms/{roomId}/allowed-products
	 */
	public Mono<Void> deleteRoomAllowedProducts(Long roomId) {
		return webClient.delete()
				.uri("/api/v1/admin/rooms/" + roomId + "/allowed-products")
				.retrieve()
				.bodyToMono(Void.class);
	}
}
