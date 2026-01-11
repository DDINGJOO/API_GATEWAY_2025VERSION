package com.study.api_gateway.api.place.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Room 검색 응답에 포함될 간략한 Place 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceInfoSummary {
	
	/**
	 * 카테고리 (예: MUSIC_STUDIO, DANCE_STUDIO 등)
	 */
	private String category;

	/**
	 * 공간명
	 */
	private String placeName;
	
	/**
	 * 공간 타입 (예: RENTAL, STUDIO 등)
	 */
	private String placeType;
	
	/**
	 * 전체 주소
	 */
	private String fullAddress;
	
	/**
	 * 주차 가능 여부
	 */
	private Boolean parkingAvailable;
}
