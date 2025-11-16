package com.study.api_gateway.dto.pricing.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 가격 정책 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPolicyResponse {
	private Long roomId;
	private Long placeId;
	private String timeSlot;
	private BigDecimal defaultPrice;
	private List<TimeRangePriceResponse> timeRangePrices;
}