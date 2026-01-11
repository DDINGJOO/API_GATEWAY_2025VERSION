package com.study.api_gateway.api.roomReservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "휴무일 설정 요청")
public record ClosedDatesRequest(
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "휴무일 목록")
		List<ClosedDate> closedDates
) {
	@Schema(description = "휴무일 정보")
	public record ClosedDate(
			@Schema(description = "시작 날짜 (yyyy-MM-dd)", example = "2025-01-01")
			String startDate,
			
			@Schema(description = "종료 날짜 (yyyy-MM-dd)", example = "2025-01-01")
			String endDate,
			
			@Schema(description = "시작 시각 (HH:mm, null이면 전일)", example = "14:00", nullable = true)
			String startTime,
			
			@Schema(description = "종료 시각 (HH:mm, null이면 전일)", example = "18:00", nullable = true)
			String endTime,
			
			@Schema(description = "휴무 사유", example = "신정", nullable = true)
			String reason
	) {
	}
}
