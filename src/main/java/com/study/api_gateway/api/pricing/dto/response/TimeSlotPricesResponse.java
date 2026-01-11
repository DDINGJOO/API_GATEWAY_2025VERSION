package com.study.api_gateway.api.pricing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "시간대별 가격 조회 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotPricesResponse {
	
	@Schema(description = "시간대별 가격 맵 (시간 -> 가격)", example = "{\"10:00\": 50000, \"11:00\": 50000}")
	private Map<String, Integer> timeSlotPrices;
}
