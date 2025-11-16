package com.study.api_gateway.dto.pricing.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 시간대별 가격 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangePriceResponse {
	private String dayOfWeek;
	private String startTime;
	private String endTime;
	private BigDecimal price;
}