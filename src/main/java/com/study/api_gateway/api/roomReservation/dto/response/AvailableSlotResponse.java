package com.study.api_gateway.api.roomReservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 가능 슬롯 정보")
public record AvailableSlotResponse(
		@Schema(description = "슬롯 ID", example = "12345")
		Long slotId,
		
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "슬롯 날짜", example = "2025-01-20")
		String slotDate,
		
		@Schema(description = "슬롯 시각 (HH:mm)", example = "09:00")
		String slotTime,
		
		@Schema(description = "슬롯 상태", example = "AVAILABLE")
		String status
) {
}
