package com.study.api_gateway.dto.roomReservation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "룸 운영 정책 설정 및 슬롯 생성 요청")
public record RoomSetupRequest(
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "요일별 슬롯 시작 시각 목록")
		List<SlotSchedule> slots
) {
	@Schema(description = "요일별 슬롯 스케줄")
	public record SlotSchedule(
			@Schema(description = "요일", example = "MONDAY")
			String dayOfWeek,
			
			@Schema(description = "시작 시각 목록 (HH:mm 형식)", example = "[\"09:00\", \"10:00\", \"11:00\"]")
			List<String> startTimes,
			
			@Schema(description = "반복 패턴", example = "EVERY_WEEK")
			String recurrencePattern
	) {
	}
}
