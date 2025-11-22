package com.study.api_gateway.dto.pricing.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "시간대별 가격 업데이트 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangePricesUpdateRequest {
	
	@Schema(description = "시간대별 가격 목록")
	private List<TimeRangePriceItem> timeRangePrices;
	
	@Schema(description = "시간대별 가격 항목")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TimeRangePriceItem {
		@Schema(description = "요일", example = "MONDAY")
		private String dayOfWeek;
		
		@Schema(description = "시작 시간", example = "18:00")
		private String startTime;
		
		@Schema(description = "종료 시간", example = "22:00")
		private String endTime;
		
		@Schema(description = "가격", example = "60000")
		private Integer price;
	}
}
