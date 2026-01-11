package com.study.api_gateway.api.room.dto.response;

import com.study.api_gateway.api.pricing.dto.response.PricingPolicyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 룸 상세 정보 + 가격 정책 응답
 * 여러 룸 일괄 조회 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailWithPricingResponse {
	
	/**
	 * 룸 상세 정보
	 */
	private RoomDetailResponse room;
	
	/**
	 * 가격 정책
	 */
	private PricingPolicyResponse pricingPolicy;
}
