package com.study.api_gateway.api.product.dto.response;

import com.study.api_gateway.api.product.dto.enums.ProductScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
	private Long productId;
	private ProductScope scope;
	private Long placeId;
	private Long roomId;
	private String name;
	private PricingStrategyDto pricingStrategy;
	private Integer totalQuantity;
}