package com.study.api_gateway.api.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "예약 상품 업데이트 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationProductsRequest {
	
	@Schema(description = "상품 목록")
	private List<ReservationPreviewRequest.ProductQuantity> products;
}
