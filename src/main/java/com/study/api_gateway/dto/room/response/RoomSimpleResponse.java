package com.study.api_gateway.dto.room.response;

import com.study.api_gateway.dto.room.enums.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
	private List<String> imageUrls;
	private List<Long> keywordIds;
}
