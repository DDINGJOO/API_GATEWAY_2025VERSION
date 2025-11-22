package com.study.api_gateway.dto.reservationManage.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 상태")
public enum ReservationStatus {
	@Schema(description = "결제 대기 중")
	PENDING_PAYMENT,
	
	@Schema(description = "예약 확정")
	CONFIRMED,
	
	@Schema(description = "예약 취소")
	CANCELLED
}
