package com.study.api_gateway.api.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "룸 허용 상품 설정 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAllowedProductsRequest {
	
	@Schema(description = "상품 ID 목록", example = "[1, 2, 3]")
	private List<Long> productIds;
}
