package com.study.api_gateway.dto.reservation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "예약 가격 정보 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPriceResponse {
	
	@Schema(description = "예약 ID", example = "1")
	private Long reservationId;
	
	@Schema(description = "룸 ID", example = "1")
	private Long roomId;
	
	@Schema(description = "예약 상태", example = "CONFIRMED")
	private String status;
	
	@Schema(description = "총 가격", example = "125000")
	private Integer totalPrice;
	
	@Schema(description = "가격 계산 시각", example = "2025-01-18T10:00:00")
	private LocalDateTime calculatedAt;
}
