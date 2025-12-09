package com.study.api_gateway.dto.room.request;

import com.study.api_gateway.dto.room.enums.TimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "방 생성 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequest {
	
	@Schema(description = "방 이름", example = "회의실 A", required = true)
	private String roomName;
	
	@Schema(description = "Place ID", example = "1", required = true)
	private Long placeId;
	
	@Schema(description = "시간 단위", example = "HOUR")
	private TimeSlot timeSlot;
	
	@Schema(description = "추가 정보 목록 (최대 7개)", example = "[\"WiFi 제공\", \"빔 프로젝터 구비\"]")
	private List<String> furtherDetails;
	
	@Schema(description = "주의 사항 목록 (최대 8개)", example = "[\"음식물 반입 금지\", \"정리정돈 필수\"]")
	private List<String> cautionDetails;
	
	@Schema(description = "키워드 ID 목록", example = "[1, 2, 3]")
	private List<Long> keywordIds;
	
	@Schema(description = "최대 수용 인원 (최소 1명 이상)", example = "10", required = true)
	private Integer maxOccupancy;
}
