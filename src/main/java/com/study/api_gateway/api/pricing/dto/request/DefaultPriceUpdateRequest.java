package com.study.api_gateway.api.pricing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "기본 가격 업데이트 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultPriceUpdateRequest {
	
	@Schema(description = "기본 가격", example = "55000")
	private Integer defaultPrice;
}
