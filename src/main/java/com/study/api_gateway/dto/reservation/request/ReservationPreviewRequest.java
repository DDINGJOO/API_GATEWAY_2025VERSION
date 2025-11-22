package com.study.api_gateway.dto.reservation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "예약 가격 미리보기 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPreviewRequest {
	
	@Schema(description = "룸 ID", example = "1")
	private Long roomId;
	
	@Schema(description = "예약 시간 슬롯 목록", example = "[\"2025-01-20T10:00:00\", \"2025-01-20T11:00:00\"]")
	private List<LocalDateTime> timeSlots;
	
	@Schema(description = "상품 목록")
	private List<ProductQuantity> products;
	
	@Schema(description = "상품 수량 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductQuantity {
		@Schema(description = "상품 ID", example = "1")
		private Long productId;
		
		@Schema(description = "수량", example = "2")
		private Integer quantity;
	}
}
