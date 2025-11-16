package com.study.api_gateway.dto.product.response;

import com.study.api_gateway.dto.product.enums.PricingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 가격 전략 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingStrategyDto {
	private PricingType pricingType;
	private BigDecimal initialPrice;
	private BigDecimal additionalPrice;
}