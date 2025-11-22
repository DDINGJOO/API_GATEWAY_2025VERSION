package com.study.api_gateway.dto.product.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "룸 허용 상품 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAllowedProductsResponse {
	
	@Schema(description = "룸 ID", example = "1")
	private Long roomId;
	
	@Schema(description = "허용된 상품 ID 목록", example = "[1, 2, 3]")
	private List<Long> allowedProductIds;
}
