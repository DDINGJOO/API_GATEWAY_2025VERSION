package com.study.api_gateway.dto.roomReservation.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "룸 운영 정책 설정 응답")
public record RoomSetupResponse(
		@Schema(description = "슬롯 생성 요청 ID", example = "abc-123-def-456")
		String requestId,
		
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "슬롯 생성 시작 날짜", example = "2025-01-17")
		String startDate,
		
		@Schema(description = "슬롯 생성 종료 날짜", example = "2025-02-16")
		String endDate,
		
		@Schema(description = "요청 상태", example = "REQUESTED")
		String status,
		
		@Schema(description = "요청 시각", example = "2025-01-17T10:30:00")
		String requestedAt
) {
}
