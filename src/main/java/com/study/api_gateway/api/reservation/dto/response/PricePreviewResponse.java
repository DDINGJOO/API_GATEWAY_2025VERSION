package com.study.api_gateway.api.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 가격 미리보기 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePreviewResponse {
	private BigDecimal timeSlotPrice;
	private List<ProductPriceDetail> productBreakdowns;
	private BigDecimal totalPrice;
}
