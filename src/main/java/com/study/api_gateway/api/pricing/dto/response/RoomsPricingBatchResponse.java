package com.study.api_gateway.api.pricing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Room ID 리스트 기반 가격 정책 배치 조회 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomsPricingBatchResponse {
	
	/**
	 * Room별 가격 정보 리스트
	 */
	private List<RoomPricingInfo> rooms;
}
