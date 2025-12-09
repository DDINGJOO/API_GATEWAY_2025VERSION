package com.study.api_gateway.dto.pricing.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Place 기반 가격 정책 배치 조회 응답
 * 특정 Place에 속한 모든 Room의 가격 정책 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacePricingBatchResponse {
	
	/**
	 * 장소 ID
	 */
	private Long placeId;
	
	/**
	 * Room별 가격 정보 리스트
	 */
	private List<RoomPricingInfo> rooms;
}
