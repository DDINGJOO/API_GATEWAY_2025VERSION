package com.study.api_gateway.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.api_gateway.dto.common.ImageInfo;
import com.study.api_gateway.dto.place.response.PlaceInfoSummary;
import com.study.api_gateway.dto.room.enums.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Room 검색 응답 (Place 정보 및 가격 정책 포함)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSearchWithPlaceResponse {
	
	/**
	 * Room ID
	 */
	private Long roomId;
	
	/**
	 * Room 이름
	 */
	private String roomName;
	
	/**
	 * Place ID
	 */
	private Long placeId;
	
	/**
	 * 시간 단위
	 */
	private TimeSlot timeSlot;
	
	/**
	 * 최대 수용 인원
	 */
	private Integer maxOccupancy;
	
	/**
	 * 평균 평점 (기본값 3.0)
	 */
	@Builder.Default
	private Double ratingAverage = 3.0;
	
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
	
	/**
	 * 키워드 ID 목록
	 */
	private List<Long> keywordIds;
	
	/**
	 * 기본 가격 (YeYakHaeYo 서버에서 조회)
	 */
	private BigDecimal defaultPrice;
	
	/**
	 * Place 정보 요약
	 */
	private PlaceInfoSummary placeInfo;
	
	/**
	 * RoomSimpleResponse로부터 RoomSearchWithPlaceResponse 생성
	 */
	public static RoomSearchWithPlaceResponse fromRoomSimple(
			RoomSimpleResponse room,
			PlaceInfoSummary placeInfo,
			BigDecimal defaultPrice,
			Double ratingAverage
	) {
		return RoomSearchWithPlaceResponse.builder()
				.roomId(room.getRoomId())
				.roomName(room.getRoomName())
				.placeId(room.getPlaceId())
				.timeSlot(room.getTimeSlot())
				.maxOccupancy(room.getMaxOccupancy())
				.images(room.getImages())
				.keywordIds(room.getKeywordIds())
				.defaultPrice(defaultPrice)
				.placeInfo(placeInfo)
				.ratingAverage(ratingAverage != null ? ratingAverage : 3.0)
				.build();
	}
	
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
