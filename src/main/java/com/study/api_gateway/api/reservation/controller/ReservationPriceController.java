package com.study.api_gateway.api.reservation.controller;

import com.study.api_gateway.api.reservation.dto.request.ReservationPreviewRequest;
import com.study.api_gateway.api.reservation.dto.request.UpdateReservationProductsRequest;
import com.study.api_gateway.api.reservation.service.ReservationFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import io.swagger.v3.oas.annotations.Parameter;
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
public class ReservationPriceController implements ReservationPriceApi {
	
	private final ReservationFacadeService reservationFacadeService;
	private final ResponseFactory responseFactory;
	
	@Override
	@PostMapping("/preview")
	public Mono<ResponseEntity<BaseResponse>> previewReservation(
			@RequestBody ReservationPreviewRequest request,
			ServerHttpRequest req
	) {
		log.info("예약 가격 미리보기: roomId={}, timeSlots count={}",
				request.getRoomId(), request.getTimeSlots().size());
		
		return reservationFacadeService.previewReservation(request)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@PutMapping("/{reservationId}/confirm")
	public Mono<ResponseEntity<BaseResponse>> confirmReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			ServerHttpRequest req
	) {
		log.info("예약 확정: reservationId={}", reservationId);
		
		return reservationFacadeService.confirmReservation(reservationId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@PutMapping("/{reservationId}/cancel")
	public Mono<ResponseEntity<BaseResponse>> cancelReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			ServerHttpRequest req
	) {
		log.info("예약 취소: reservationId={}", reservationId);
		
		return reservationFacadeService.cancelReservation(reservationId)
				.map(response -> responseFactory.ok(response, req));
	}
	
	@Override
	@PutMapping("/{reservationId}/products")
	public Mono<ResponseEntity<BaseResponse>> updateReservationProducts(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			@RequestBody UpdateReservationProductsRequest request,
			ServerHttpRequest req
	) {
		log.info("예약 상품 업데이트: reservationId={}, products count={}",
				reservationId, request.getProducts().size());
		
		return reservationFacadeService.updateReservationProducts(reservationId, request)
				.map(response -> responseFactory.ok(response, req));
	}
}
