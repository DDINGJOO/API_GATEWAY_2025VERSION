package com.study.api_gateway.dto.reservationManage.enums;

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
