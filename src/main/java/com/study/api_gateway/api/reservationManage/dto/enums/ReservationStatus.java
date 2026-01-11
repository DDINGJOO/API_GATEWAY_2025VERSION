package com.study.api_gateway.api.reservationManage.dto.enums;

public enum ReservationStatus {
	AWAITING_USER_INFO,
	PENDING,
	PENDING_CONFIRMED,
	PENDING_PAYMENT,
	CONFIRMED,
	REJECTED,
	REFUNDED,
	CANCELLED
}
