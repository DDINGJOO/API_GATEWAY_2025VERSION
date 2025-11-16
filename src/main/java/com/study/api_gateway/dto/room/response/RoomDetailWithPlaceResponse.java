package com.study.api_gateway.dto.room.response;

import com.study.api_gateway.dto.place.response.PlaceInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 룸 상세 정보 + 장소 정보 통합 응답
 * BFF 레이어에서 Room Server와 PlaceInfo Server의 데이터를 결합
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
}
