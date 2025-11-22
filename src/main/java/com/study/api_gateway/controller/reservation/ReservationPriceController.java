package com.study.api_gateway.controller.reservation;

import com.study.api_gateway.client.YeYakHaeYoClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.reservation.request.ReservationPreviewRequest;
import com.study.api_gateway.dto.reservation.request.UpdateReservationProductsRequest;
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

/**
 * 클라이언트 앱용 예약 가격 관리 API
 * RESTful 방식의 예약 가격 CRUD 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Price", description = "예약 가격 관리 API")
public class ReservationPriceController {
	
	private final YeYakHaeYoClient yeYakHaeYoClient;
	private final ResponseFactory responseFactory;
	
	/**
	 * 예약 가격 미리보기
	 * POST /bff/v1/reservations/preview
	 */
	@PostMapping("/preview")
	@Operation(summary = "예약 가격 미리보기", description = "예약 시 예상되는 가격을 미리 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ReservationPreviewSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"roomId\": 101,\n    \"placeId\": 1,\n    \"basePrice\": 45000,\n    \"productPrice\": 30000,\n    \"totalPrice\": 75000,\n    \"timeSlots\": [\n      \"2025-01-16T14:00:00\",\n      \"2025-01-16T15:00:00\",\n      \"2025-01-16T16:00:00\"\n    ],\n    \"products\": [\n      {\n        \"productId\": 1,\n        \"productName\": \"빔 프로젝터\",\n        \"quantity\": 1,\n        \"unitPrice\": 10000,\n        \"totalPrice\": 10000\n      },\n      {\n        \"productId\": 2,\n        \"productName\": \"화이트보드\",\n        \"quantity\": 2,\n        \"unitPrice\": 5000,\n        \"totalPrice\": 10000\n      }\n    ]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/reservations/preview\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> previewReservation(
			@RequestBody ReservationPreviewRequest request,
			ServerHttpRequest req
	) {
		log.info("예약 가격 미리보기: roomId={}, timeSlots count={}",
				request.getRoomId(), request.getTimeSlots().size());
		
		return yeYakHaeYoClient.previewReservation(request)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 예약 확정
	 * PUT /bff/v1/reservations/{reservationId}/confirm
	 */
	@PutMapping("/{reservationId}/confirm")
	@Operation(summary = "예약 확정", description = "임시 예약을 확정하고 최종 가격을 확정합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "확정 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ConfirmReservationSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"reservationId\": 1,\n    \"roomId\": 101,\n    \"placeId\": 1,\n    \"basePrice\": 45000,\n    \"productPrice\": 30000,\n    \"totalPrice\": 75000,\n    \"status\": \"CONFIRMED\",\n    \"confirmedAt\": \"2025-01-16T10:30:00\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/reservations/1/confirm\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> confirmReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			ServerHttpRequest req
	) {
		log.info("예약 확정: reservationId={}", reservationId);
		
		return yeYakHaeYoClient.confirmReservation(reservationId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 예약 취소
	 * PUT /bff/v1/reservations/{reservationId}/cancel
	 */
	@PutMapping("/{reservationId}/cancel")
	@Operation(summary = "예약 취소", description = "예약을 취소하고 환불 금액을 계산합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "취소 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "CancelReservationSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"reservationId\": 1,\n    \"roomId\": 101,\n    \"placeId\": 1,\n    \"originalPrice\": 75000,\n    \"refundAmount\": 60000,\n    \"cancellationFee\": 15000,\n    \"status\": \"CANCELLED\",\n    \"cancelledAt\": \"2025-01-16T11:00:00\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/reservations/1/cancel\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> cancelReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			ServerHttpRequest req
	) {
		log.info("예약 취소: reservationId={}", reservationId);
		
		return yeYakHaeYoClient.cancelReservation(reservationId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	/**
	 * 예약 상품 업데이트
	 * PUT /bff/v1/reservations/{reservationId}/products
	 */
	@PutMapping("/{reservationId}/products")
	@Operation(summary = "예약 상품 업데이트", description = "예약에 포함된 추가 상품을 수정하고 가격을 재계산합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "UpdateProductsSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"reservationId\": 1,\n    \"roomId\": 101,\n    \"placeId\": 1,\n    \"basePrice\": 45000,\n    \"productPrice\": 40000,\n    \"totalPrice\": 85000,\n    \"status\": \"CONFIRMED\",\n    \"updatedAt\": \"2025-01-16T12:00:00\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/reservations/1/products\"\n  }\n}")))
	})
	public Mono<ResponseEntity<BaseResponse>> updateReservationProducts(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			@RequestBody UpdateReservationProductsRequest request,
			ServerHttpRequest req
	) {
		log.info("예약 상품 업데이트: reservationId={}, products count={}",
				reservationId, request.getProducts().size());
		
		return yeYakHaeYoClient.updateReservationProducts(reservationId, request)
				.map(response -> responseFactory.ok(response, req));
	}
}
