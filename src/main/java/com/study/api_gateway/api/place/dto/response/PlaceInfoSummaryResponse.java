package com.study.api_gateway.api.place.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.api_gateway.api.place.dto.enums.ApprovalStatus;
import com.study.api_gateway.common.response.ImageInfo;
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
	
	/**
	 * 첫 번째 이미지 정보 (대표 이미지)
	 */
	private ImageInfo firstImage;
	
	/**
	 * 기존 썸네일 URL (하위 호환성을 위해 유지)
	 *
	 * @deprecated 향후 제거 예정. firstImage 필드 사용 권장
	 */
	@Deprecated
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String thumbnailUrl;
	
	private String shortAddress;
	private Boolean parkingAvailable;
	
	private Double ratingAverage;
	private Integer reviewCount;
	private ApprovalStatus approvalStatus;
	private Boolean isActive;
	
	/**
	 * firstImage로부터 thumbnailUrl를 자동 생성 (하위 호환성)
	 */
	public String getThumbnailUrl() {
		if (firstImage != null && firstImage.getImageUrl() != null) {
			return firstImage.getImageUrl();
		}
		return thumbnailUrl;
	}
}
