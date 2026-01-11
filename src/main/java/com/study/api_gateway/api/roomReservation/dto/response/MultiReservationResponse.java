package com.study.api_gateway.api.roomReservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "다중 슬롯 예약 응답")
public record MultiReservationResponse(
		@Schema(description = "예약 ID", example = "567890123456789")
		Long reservationId,
		
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "슬롯 날짜", example = "2025-01-20")
		String slotDate,
		
		@Schema(description = "예약된 슬롯 시각 목록", example = "[\"14:00\", \"15:00\", \"16:00\"]")
		List<String> reservedSlotTimes
) {
}
