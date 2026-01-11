package com.study.api_gateway.api.reservation.client;

import com.study.api_gateway.api.pricing.dto.request.CopyPricingPolicyRequest;
import com.study.api_gateway.api.pricing.dto.request.DefaultPriceUpdateRequest;
import com.study.api_gateway.api.pricing.dto.request.RoomsPricingBatchRequest;
import com.study.api_gateway.api.pricing.dto.request.TimeRangePricesUpdateRequest;
import com.study.api_gateway.api.pricing.dto.response.PlacePricingBatchResponse;
import com.study.api_gateway.api.pricing.dto.response.PricingPolicyResponse;
import com.study.api_gateway.api.pricing.dto.response.RoomsPricingBatchResponse;
import com.study.api_gateway.api.pricing.dto.response.TimeSlotPricesResponse;
import com.study.api_gateway.api.product.dto.enums.ProductScope;
import com.study.api_gateway.api.product.dto.request.ProductCreateRequest;
import com.study.api_gateway.api.product.dto.request.ProductUpdateRequest;
import com.study.api_gateway.api.product.dto.request.RoomAllowedProductsRequest;
import com.study.api_gateway.api.product.dto.response.ProductAvailabilityResponse;
import com.study.api_gateway.api.product.dto.response.ProductResponse;
import com.study.api_gateway.api.product.dto.response.RoomAllowedProductsResponse;
import com.study.api_gateway.api.reservation.dto.request.ReservationPreviewRequest;
import com.study.api_gateway.api.reservation.dto.request.UpdateReservationProductsRequest;
import com.study.api_gateway.api.reservation.dto.response.ReservationPreviewResponse;
import com.study.api_gateway.api.reservation.dto.response.ReservationPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * YeYakHaeYo Server와 통신하는 WebClient 기반 클라이언트
 * 상품, 가격 정책, 예약 가격 관리 API 제공
 */
@Slf4j
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/products")
						.build())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ProductResponse.class);
	}
	
	/**
	 * 가격 정책 조회
	 * GET /api/v1/pricing-policies/{roomId}
	 */
	public Mono<PricingPolicyResponse> getPricingPolicy(Long roomId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/pricing-policies/{roomId}")
						.build(roomId))
				.retrieve()
				.bodyToMono(PricingPolicyResponse.class);
	}
	
	/**
	 * 상품 ID로 조회
	 * GET /api/v1/products/{productId}
	 */
	public Mono<ProductResponse> getProductById(Long productId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/products/{productId}")
						.build(productId))
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
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path("/api/v1/products");
					
					if (scope != null) uriBuilder.queryParam("scope", scope);
					if (placeId != null) uriBuilder.queryParam("placeId", placeId);
					if (roomId != null) uriBuilder.queryParam("roomId", roomId);
					
					return uriBuilder.build();
				})
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
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path("/api/v1/products/availability");
					
					uriBuilder.queryParam("roomId", roomId);
					uriBuilder.queryParam("placeId", placeId);
					if (timeSlots != null && !timeSlots.isEmpty()) {
						timeSlots.forEach(slot -> uriBuilder.queryParam("timeSlots", slot.toString()));
					}
					
					return uriBuilder.build();
				})
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
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path("/api/v1/rooms/{roomId}/available-products");
					uriBuilder.queryParam("placeId", placeId);
					
					return uriBuilder.build(roomId);
				})
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/products/{productId}")
						.build(productId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/products/{productId}")
						.build(productId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/pricing-policies/{roomId}/date/{date}")
						.build(roomId, date.toString()))
				.retrieve()
				.bodyToMono(TimeSlotPricesResponse.class);
	}
	
	/**
	 * 기본 가격 업데이트
	 * PUT /api/v1/pricing-policies/{roomId}/default-price
	 */
	public Mono<PricingPolicyResponse> updateDefaultPrice(Long roomId, DefaultPriceUpdateRequest request) {
		return webClient.put()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/pricing-policies/{roomId}/default-price")
						.build(roomId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/pricing-policies/{roomId}/time-range-prices")
						.build(roomId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/pricing-policies/{targetRoomId}/copy")
						.build(targetRoomId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/reservations/preview")
						.build())
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/reservations/{reservationId}/confirm")
						.build(reservationId))
				.retrieve()
				.bodyToMono(ReservationPriceResponse.class);
	}
	
	/**
	 * 예약 취소
	 * PUT /api/v1/reservations/{reservationId}/cancel
	 */
	public Mono<ReservationPriceResponse> cancelReservation(Long reservationId) {
		return webClient.put()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/reservations/{reservationId}/cancel")
						.build(reservationId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/reservations/{reservationId}/products")
						.build(reservationId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/admin/rooms/{roomId}/allowed-products")
						.build(roomId))
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
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/admin/rooms/{roomId}/allowed-products")
						.build(roomId))
				.retrieve()
				.bodyToMono(RoomAllowedProductsResponse.class);
	}
	
	/**
	 * 룸 허용 상품 삭제
	 * DELETE /api/v1/admin/rooms/{roomId}/allowed-products
	 */
	public Mono<Void> deleteRoomAllowedProducts(Long roomId) {
		return webClient.delete()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/admin/rooms/{roomId}/allowed-products")
						.build(roomId))
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	// ========== 배치 가격 정책 조회 API ==========
	
	/**
	 * Place ID 기반 가격 정책 배치 조회
	 * GET /api/v1/pricing-policies/places/{placeId}/batch
	 *
	 * @param placeId 장소 ID
	 * @param date    시간대별 가격 조회 날짜 (선택사항)
	 * @return 해당 Place의 모든 Room 가격 정책
	 */
	public Mono<PlacePricingBatchResponse> getPricingPoliciesByPlaceId(Long placeId, LocalDate date) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path("/api/v1/pricing-policies/places/{placeId}/batch");
					if (date != null) {
						uriBuilder.queryParam("date", date.toString());
					}
					return uriBuilder.build(placeId);
				})
				.retrieve()
				.bodyToMono(PlacePricingBatchResponse.class)
				.onErrorResume(error -> {
					log.error("Place 기반 가격 정책 배치 조회 실패: placeId={}, error={}", placeId, error.getMessage());
					// 에러 시 빈 결과 반환
					return Mono.just(PlacePricingBatchResponse.builder()
							.placeId(placeId)
							.rooms(List.of())
							.build());
				});
	}
	
	/**
	 * Room ID 리스트 기반 가격 정책 배치 조회
	 * POST /api/v1/pricing-policies/rooms/batch
	 *
	 * @param roomIds 조회할 Room ID 리스트
	 * @param date    시간대별 가격 조회 날짜 (선택사항)
	 * @return Room별 가격 정책 목록
	 */
	public Mono<RoomsPricingBatchResponse> getPricingPoliciesByRoomIds(List<Long> roomIds, LocalDate date) {
		if (roomIds == null || roomIds.isEmpty()) {
			return Mono.just(RoomsPricingBatchResponse.builder()
					.rooms(List.of())
					.build());
		}
		
		RoomsPricingBatchRequest request = RoomsPricingBatchRequest.builder()
				.roomIds(roomIds)
				.date(date)
				.build();
		
		return webClient.post()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/pricing-policies/rooms/batch")
						.build())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(RoomsPricingBatchResponse.class)
				.onErrorResume(error -> {
					log.error("Room ID 기반 가격 정책 배치 조회 실패: roomIds={}, error={}", roomIds, error.getMessage());
					// 에러 시 빈 결과 반환
					return Mono.just(RoomsPricingBatchResponse.builder()
							.rooms(List.of())
							.build());
				});
	}
	
	/**
	 * Room ID 리스트 기반 가격 정책 배치 조회 (기본 가격만)
	 *
	 * @param roomIds 조회할 Room ID 리스트
	 * @return Room별 기본 가격 정책 목록
	 */
	public Mono<RoomsPricingBatchResponse> getPricingPoliciesByRoomIds(List<Long> roomIds) {
		return getPricingPoliciesByRoomIds(roomIds, null);
	}
}
