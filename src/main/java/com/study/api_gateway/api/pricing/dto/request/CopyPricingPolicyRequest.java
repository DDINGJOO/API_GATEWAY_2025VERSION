package com.study.api_gateway.api.pricing.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "가격 정책 복사 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopyPricingPolicyRequest {
	
	@Schema(description = "원본 룸 ID", example = "2")
	private Long sourceRoomId;
}
