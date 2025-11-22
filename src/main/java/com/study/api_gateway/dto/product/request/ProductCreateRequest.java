package com.study.api_gateway.dto.product.request;

import com.study.api_gateway.dto.product.enums.ProductScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "상품 등록 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
	
	@Schema(description = "상품 적용 범위", example = "PLACE")
	private ProductScope scope;
	
	@Schema(description = "플레이스 ID", example = "1")
	private Long placeId;
	
	@Schema(description = "룸 ID", example = "1")
	private Long roomId;
	
	@Schema(description = "상품명", example = "상품명")
	private String name;
	
	@Schema(description = "가격 전략")
	private PricingStrategyRequest pricingStrategy;
	
	@Schema(description = "총 수량", example = "10")
	private Integer totalQuantity;
	
	@Schema(description = "가격 전략 요청")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PricingStrategyRequest {
		@Schema(description = "가격 전략 타입", example = "ONE_TIME")
		private String pricingType;
		
		@Schema(description = "초기 가격", example = "10000")
		private Integer initialPrice;
		
		@Schema(description = "추가 가격", example = "5000")
		private Integer additionalPrice;
	}
}
