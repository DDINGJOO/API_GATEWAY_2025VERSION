package com.study.api_gateway.dto.roomReservation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "휴무일 설정 응답")
public record ClosedDatesResponse(
		@Schema(description = "요청 ID", example = "def-456-ghi-789")
		String requestId,
		
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "휴무일 개수", example = "2")
		Integer closedDateCount,
		
		@Schema(description = "요청 상태", example = "REQUESTED")
		String status,
		
		@Schema(description = "요청 시각", example = "2025-01-17T10:30:00")
		String requestedAt
) {
}
