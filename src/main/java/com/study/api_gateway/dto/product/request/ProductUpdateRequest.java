package com.study.api_gateway.dto.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "상품 수정 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
	
	@Schema(description = "상품명", example = "수정된 상품명")
	private String name;
	
	@Schema(description = "가격 전략")
	private ProductCreateRequest.PricingStrategyRequest pricingStrategy;
	
	@Schema(description = "총 수량", example = "20")
	private Integer totalQuantity;
}
