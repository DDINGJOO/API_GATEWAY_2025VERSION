package com.study.api_gateway.api.reservation.controller;

import com.study.api_gateway.api.reservation.dto.request.ReservationPreviewRequest;
import com.study.api_gateway.api.reservation.dto.request.UpdateReservationProductsRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

/**
 * 예약 가격 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Reservation Price", description = "예약 가격 관리 API")
public interface ReservationPriceApi {
	
	@Operation(summary = "예약 가격 미리보기", description = "예약 시 예상되는 가격을 미리 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PostMapping("/preview")
	Mono<ResponseEntity<BaseResponse>> previewReservation(
			@RequestBody ReservationPreviewRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "예약 확정", description = "임시 예약을 확정하고 최종 가격을 확정합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "확정 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PutMapping("/{reservationId}/confirm")
	Mono<ResponseEntity<BaseResponse>> confirmReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			ServerHttpRequest req);
	
	@Operation(summary = "예약 취소", description = "예약을 취소하고 환불 금액을 계산합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "취소 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PutMapping("/{reservationId}/cancel")
	Mono<ResponseEntity<BaseResponse>> cancelReservation(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			ServerHttpRequest req);
	
	@Operation(summary = "예약 상품 업데이트", description = "예약에 포함된 추가 상품을 수정하고 가격을 재계산합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PutMapping("/{reservationId}/products")
	Mono<ResponseEntity<BaseResponse>> updateReservationProducts(
			@Parameter(description = "예약 ID", required = true) @PathVariable Long reservationId,
			@RequestBody UpdateReservationProductsRequest request,
			ServerHttpRequest req);
}
