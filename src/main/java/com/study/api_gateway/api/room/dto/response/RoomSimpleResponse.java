package com.study.api_gateway.api.room.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.api_gateway.api.room.dto.enums.TimeSlot;
import com.study.api_gateway.common.response.ImageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 룸 간단 정보 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSimpleResponse {
	
	private Long roomId;
	private String roomName;
	private Long placeId;
	private TimeSlot timeSlot;
	private Integer maxOccupancy;
	
	/**
	 * 새로운 이미지 구조 (imageId, imageUrl, sequence 포함)
	 */
	private List<ImageInfo> images;
	
	/**
	 * 기존 이미지 URL 목록 (하위 호환성을 위해 유지)
	 *
	 * @deprecated 향후 제거 예정. images 필드 사용 권장
	 */
	@Deprecated
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private List<String> imageUrls;
	
	private List<Long> keywordIds;
	
	/**
	 * images 필드로부터 imageUrls를 자동 생성 (하위 호환성)
	 */
	public List<String> getImageUrls() {
		if (images == null || images.isEmpty()) {
			return imageUrls != null ? imageUrls : Collections.emptyList();
		}
		return images.stream()
				.map(ImageInfo::getImageUrl)
				.collect(Collectors.toList());
	}
}
