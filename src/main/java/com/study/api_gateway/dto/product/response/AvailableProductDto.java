package com.study.api_gateway.dto.product.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 가용한 상품 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableProductDto {
	private Long productId;
	private String productName;
	private BigDecimal unitPrice;
	private Integer availableQuantity;
	private Integer totalStock;
}