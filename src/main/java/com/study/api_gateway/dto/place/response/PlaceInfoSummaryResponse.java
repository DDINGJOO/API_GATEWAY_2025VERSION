package com.study.api_gateway.dto.place.response;

import com.study.api_gateway.dto.place.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 요약 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfoSummaryResponse {
	
	private String id;
	private String placeName;
	private String category;
	private String placeType;
	private String thumbnailUrl;
	
	private String shortAddress;
	private Boolean parkingAvailable;
	
	private Double ratingAverage;
	private Integer reviewCount;
	private ApprovalStatus approvalStatus;
	private Boolean isActive;
}
