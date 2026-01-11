package com.study.api_gateway.api.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "예약 가격 미리보기 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPreviewResponse {
	
	@Schema(description = "시간 슬롯 가격", example = "100000")
	private Integer timeSlotPrice;
	
	@Schema(description = "상품별 가격 내역")
	private List<ProductBreakdown> productBreakdowns;
	
	@Schema(description = "총 가격", example = "125000")
	private Integer totalPrice;
	
	@Schema(description = "상품 가격 내역")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductBreakdown {
		@Schema(description = "상품 ID", example = "1")
		private Long productId;
		
		@Schema(description = "상품명", example = "상품명1")
		private String productName;
		
		@Schema(description = "수량", example = "2")
		private Integer quantity;
		
		@Schema(description = "단가", example = "10000")
		private Integer unitPrice;
		
		@Schema(description = "소계", example = "20000")
		private Integer subtotal;
	}
}
