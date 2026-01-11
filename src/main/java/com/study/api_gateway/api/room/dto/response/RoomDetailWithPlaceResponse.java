package com.study.api_gateway.api.room.dto.response;

import com.study.api_gateway.api.place.dto.response.PlaceInfoResponse;
import com.study.api_gateway.api.pricing.dto.response.PricingPolicyResponse;
import com.study.api_gateway.api.product.dto.response.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 룸 상세 정보 + 장소 정보 + 가격 정책 + 이용 가능 상품 통합 응답
 * BFF 레이어에서 Room Server, PlaceInfo Server, YeYakHaeYo Server의 데이터를 결합
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailWithPlaceResponse {
	
	/**
	 * 룸 상세 정보
	 */
	private RoomDetailResponse room;
	
	/**
	 * 장소 상세 정보
	 */
	private PlaceInfoResponse place;
	
	/**
	 * 가격 정책
	 */
	private PricingPolicyResponse pricingPolicy;
	
	/**
	 * 이용 가능한 상품 목록
	 */
	private List<ProductResponse> availableProducts;
}
