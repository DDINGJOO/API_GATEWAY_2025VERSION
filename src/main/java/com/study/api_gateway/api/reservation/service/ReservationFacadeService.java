package com.study.api_gateway.api.reservation.service;

import com.study.api_gateway.api.pricing.dto.request.CopyPricingPolicyRequest;
import com.study.api_gateway.api.pricing.dto.request.DefaultPriceUpdateRequest;
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
import com.study.api_gateway.api.reservation.client.YeYakHaeYoClient;
import com.study.api_gateway.api.reservation.dto.request.ReservationPreviewRequest;
import com.study.api_gateway.api.reservation.dto.request.UpdateReservationProductsRequest;
import com.study.api_gateway.api.reservation.dto.response.ReservationPreviewResponse;
import com.study.api_gateway.api.reservation.dto.response.ReservationPriceResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reservation(YeYakHaeYo) 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class ReservationFacadeService {
	
	private static final String SERVICE_NAME = "reservation-service";
	private final YeYakHaeYoClient yeYakHaeYoClient;
	private final ResilienceOperator resilience;
	
	// ========== 상품 관리 API ==========
	
	public Mono<ProductResponse> createProduct(ProductCreateRequest request) {
		return yeYakHaeYoClient.createProduct(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<PricingPolicyResponse> getPricingPolicy(Long roomId) {
		return yeYakHaeYoClient.getPricingPolicy(roomId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ProductResponse> getProductById(Long productId) {
		return yeYakHaeYoClient.getProductById(productId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<ProductResponse>> getProducts(
			ProductScope scope,
			Long placeId,
			Long roomId
	) {
		return yeYakHaeYoClient.getProducts(scope, placeId, roomId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ProductAvailabilityResponse> getProductAvailability(
			Long roomId,
			Long placeId,
			List<LocalDateTime> timeSlots
	) {
		return yeYakHaeYoClient.getProductAvailability(roomId, placeId, timeSlots)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<ProductResponse>> getAvailableProductsForRoom(Long roomId, Long placeId) {
		return yeYakHaeYoClient.getAvailableProductsForRoom(roomId, placeId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ProductResponse> updateProduct(Long productId, ProductUpdateRequest request) {
		return yeYakHaeYoClient.updateProduct(productId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> deleteProduct(Long productId) {
		return yeYakHaeYoClient.deleteProduct(productId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ========== 가격 정책 관리 API ==========
	
	public Mono<TimeSlotPricesResponse> getTimeSlotPrices(Long roomId, LocalDate date) {
		return yeYakHaeYoClient.getTimeSlotPrices(roomId, date)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<PricingPolicyResponse> updateDefaultPrice(Long roomId, DefaultPriceUpdateRequest request) {
		return yeYakHaeYoClient.updateDefaultPrice(roomId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<PricingPolicyResponse> updateTimeRangePrices(Long roomId, TimeRangePricesUpdateRequest request) {
		return yeYakHaeYoClient.updateTimeRangePrices(roomId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<PricingPolicyResponse> copyPricingPolicy(Long targetRoomId, CopyPricingPolicyRequest request) {
		return yeYakHaeYoClient.copyPricingPolicy(targetRoomId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ========== 예약 가격 관리 API ==========
	
	public Mono<ReservationPreviewResponse> previewReservation(ReservationPreviewRequest request) {
		return yeYakHaeYoClient.previewReservation(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ReservationPriceResponse> confirmReservation(Long reservationId) {
		return yeYakHaeYoClient.confirmReservation(reservationId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ReservationPriceResponse> cancelReservation(Long reservationId) {
		return yeYakHaeYoClient.cancelReservation(reservationId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<ReservationPriceResponse> updateReservationProducts(
			Long reservationId,
			UpdateReservationProductsRequest request
	) {
		return yeYakHaeYoClient.updateReservationProducts(reservationId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ========== 룸 허용 상품 관리 API (Admin) ==========
	
	public Mono<RoomAllowedProductsResponse> setRoomAllowedProducts(Long roomId, RoomAllowedProductsRequest request) {
		return yeYakHaeYoClient.setRoomAllowedProducts(roomId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<RoomAllowedProductsResponse> getRoomAllowedProducts(Long roomId) {
		return yeYakHaeYoClient.getRoomAllowedProducts(roomId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> deleteRoomAllowedProducts(Long roomId) {
		return yeYakHaeYoClient.deleteRoomAllowedProducts(roomId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	// ========== 배치 가격 정책 조회 API ==========
	
	public Mono<PlacePricingBatchResponse> getPricingPoliciesByPlaceId(Long placeId, LocalDate date) {
		return yeYakHaeYoClient.getPricingPoliciesByPlaceId(placeId, date)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<RoomsPricingBatchResponse> getPricingPoliciesByRoomIds(List<Long> roomIds, LocalDate date) {
		return yeYakHaeYoClient.getPricingPoliciesByRoomIds(roomIds, date)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<RoomsPricingBatchResponse> getPricingPoliciesByRoomIds(List<Long> roomIds) {
		return yeYakHaeYoClient.getPricingPoliciesByRoomIds(roomIds)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
