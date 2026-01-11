package com.study.api_gateway.api.roomReservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "슬롯 생성 상태 조회 응답")
public record SlotGenerationStatusResponse(
		@Schema(description = "슬롯 생성 요청 ID", example = "abc-123-def-456")
		String requestId,
		
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "요청 상태", example = "COMPLETED")
		String status,
		
		@Schema(description = "생성된 슬롯 개수", example = "150", nullable = true)
		Integer totalSlotsGenerated,
		
		@Schema(description = "슬롯 생성 시작 날짜", example = "2025-01-17")
		String startDate,
		
		@Schema(description = "슬롯 생성 종료 날짜", example = "2025-02-16")
		String endDate,
		
		@Schema(description = "요청 시각", example = "2025-01-17T10:30:00")
		String requestedAt,
		
		@Schema(description = "완료 시각", example = "2025-01-17T10:31:00", nullable = true)
		String completedAt,
		
		@Schema(description = "실패 시각", example = "2025-01-17T10:31:00", nullable = true)
		String failedAt,
		
		@Schema(description = "에러 메시지", nullable = true)
		String errorMessage
) {
}
