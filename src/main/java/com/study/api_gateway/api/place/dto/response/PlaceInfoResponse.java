package com.study.api_gateway.api.place.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.api_gateway.api.place.dto.enums.ApprovalStatus;
import com.study.api_gateway.common.response.ImageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 장소 상세 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfoResponse {
	
	private String id;
	private String userId;
	private String placeName;
	private String description;
	private String category;
	private String placeType;
	
	private PlaceContactResponse contact;
	private PlaceLocationResponse location;
	private PlaceParkingResponse parking;
	
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
	
	private List<KeywordResponse> keywords;
	
	private Boolean isActive;
	private ApprovalStatus approvalStatus;
	private Double ratingAverage;
	private Integer reviewCount;
	
	// 등록 상태 (REGISTERED: 등록 업체, UNREGISTERED: 미등록 업체)
	private String registrationStatus;
	
	// Room 정보
	private Integer roomCount;
	private List<Long> roomIds;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
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
