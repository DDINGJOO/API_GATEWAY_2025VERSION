package com.study.api_gateway.api.roomReservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "다중 슬롯 예약 요청")
public record MultiReservationRequest(
		@Schema(description = "룸 ID", example = "101")
		Long roomId,
		
		@Schema(description = "슬롯 날짜 (yyyy-MM-dd)", example = "2025-01-20")
		String slotDate,
		
		@Schema(description = "슬롯 시각 목록 (HH:mm)", example = "[\"14:00\", \"15:00\", \"16:00\"]")
		List<String> slotTimes
) {
}
