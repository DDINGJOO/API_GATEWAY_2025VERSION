package com.study.api_gateway.api.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 상품 재고 가용성 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailabilityResponse {
	private Long roomId;
	private Long placeId;
	private List<AvailableProductDto> availableProducts;
}