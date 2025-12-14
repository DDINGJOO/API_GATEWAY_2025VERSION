package com.study.api_gateway.dto.reservationManage.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum ReservationStatus {
	AWAITING_USER_INFO,  // 추가
	PENDING,             // 추가
	PENDING_PAYMENT,     // 기존
	CONFIRMED,           // 기존
	REJECTED,            // 추가
	REFUNDED,            // 추가
	CANCELLED            // 기존
}
