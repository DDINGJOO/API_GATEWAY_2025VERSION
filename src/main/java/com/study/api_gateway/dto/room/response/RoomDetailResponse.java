package com.study.api_gateway.dto.room.response;

import com.study.api_gateway.dto.room.enums.RoomStatus;
import com.study.api_gateway.dto.room.enums.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 룸 상세 정보 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailResponse {
	
	private Long roomId;
	private String roomName;
	private Long placeId;
	private RoomStatus status;
	private TimeSlot timeSlot;
	private List<String> furtherDetails;
	private List<String> cautionDetails;
	private List<String> imageUrls;
	private List<Long> keywordIds;
}
