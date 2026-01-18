package com.study.api_gateway.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Gateway 에러 코드 정의
 * <p>
 * 코드 체계:
 * - C0XX: Common (공통)
 * - A0XX: Authentication (인증)
 * - Z0XX: Authorization (인가)
 * - G0XX: Gateway (게이트웨이)
 * - R0XX: Rate Limit (속도 제한)
 * - V0XX: Validation (유효성 검사)
 * - P0XX: Profile (프로필)
 * - B0XX: Booking/Reservation (예약)
 * - L0XX: Place (장소)
 * - M0XX: Chat/Message (채팅)
 * - N0XX: Notification (알림)
 * - U0XX: Coupon (쿠폰)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	
	// ==================== Common (C0XX) ====================
	INTERNAL_ERROR("C001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_REQUEST("C002", "Invalid request", HttpStatus.BAD_REQUEST),
	RESOURCE_NOT_FOUND("C003", "Resource not found", HttpStatus.NOT_FOUND),
	DUPLICATE_RESOURCE("C004", "Resource already exists", HttpStatus.CONFLICT),
	INVALID_PARAMETER("C005", "Invalid parameter", HttpStatus.BAD_REQUEST),
	
	// ==================== Authentication (A0XX) ====================
	UNAUTHORIZED("A001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
	INVALID_TOKEN("A002", "Invalid token", HttpStatus.UNAUTHORIZED),
	TOKEN_EXPIRED("A003", "Token has expired", HttpStatus.UNAUTHORIZED),
	MISSING_TOKEN("A004", "Token is missing", HttpStatus.UNAUTHORIZED),
	INVALID_CREDENTIALS("A005", "Invalid credentials", HttpStatus.UNAUTHORIZED),
	
	// ==================== Authorization (Z0XX) ====================
	FORBIDDEN("Z001", "Access denied", HttpStatus.FORBIDDEN),
	INSUFFICIENT_PERMISSIONS("Z002", "Insufficient permissions", HttpStatus.FORBIDDEN),
	NOT_OWNER("Z003", "You are not the owner of this resource", HttpStatus.FORBIDDEN),
	
	// ==================== Gateway (G0XX) ====================
	SERVICE_UNAVAILABLE("G001", "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
	GATEWAY_TIMEOUT("G002", "Gateway timeout", HttpStatus.GATEWAY_TIMEOUT),
	BAD_GATEWAY("G003", "Bad gateway", HttpStatus.BAD_GATEWAY),
	CIRCUIT_BREAKER_OPEN("G004", "Service is temporarily unavailable due to high failure rate", HttpStatus.SERVICE_UNAVAILABLE),
	
	// ==================== Rate Limit (R0XX) ====================
	RATE_LIMIT_EXCEEDED("R001", "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),
	TOO_MANY_REQUESTS("R002", "Too many requests. Please try again later", HttpStatus.TOO_MANY_REQUESTS),
	
	// ==================== Validation (V0XX) ====================
	VALIDATION_FAILED("V001", "Validation failed", HttpStatus.BAD_REQUEST),
	REQUIRED_FIELD_MISSING("V002", "Required field is missing", HttpStatus.BAD_REQUEST),
	INVALID_FORMAT("V003", "Invalid format", HttpStatus.BAD_REQUEST),
	VALUE_OUT_OF_RANGE("V004", "Value is out of allowed range", HttpStatus.BAD_REQUEST),
	
	// ==================== Profile (P0XX) ====================
	PROFILE_NOT_FOUND("P001", "Profile not found", HttpStatus.NOT_FOUND),
	NICKNAME_DUPLICATED("P002", "Nickname already exists", HttpStatus.CONFLICT),
	PROFILE_UPDATE_FAILED("P003", "Failed to update profile", HttpStatus.INTERNAL_SERVER_ERROR),
	
	// ==================== Booking/Reservation (B0XX) ====================
	RESERVATION_NOT_FOUND("B001", "Reservation not found", HttpStatus.NOT_FOUND),
	SLOT_NOT_AVAILABLE("B002", "Time slot is not available", HttpStatus.CONFLICT),
	RESERVATION_ALREADY_EXISTS("B003", "Reservation already exists", HttpStatus.CONFLICT),
	RESERVATION_CANCELLED("B004", "Reservation has been cancelled", HttpStatus.GONE),
	INVALID_RESERVATION_STATUS("B005", "Invalid reservation status", HttpStatus.BAD_REQUEST),
	PHONE_NUMBER_REQUIRED("B006", "Phone number registration is required", HttpStatus.PRECONDITION_FAILED),
	
	// ==================== Place (L0XX) ====================
	PLACE_NOT_FOUND("L001", "Place not found", HttpStatus.NOT_FOUND),
	ROOM_NOT_FOUND("L002", "Room not found", HttpStatus.NOT_FOUND),
	PLACE_INACTIVE("L003", "Place is currently inactive", HttpStatus.GONE),
	
	// ==================== Chat/Message (M0XX) ====================
	CHAT_ROOM_NOT_FOUND("M001", "Chat room not found", HttpStatus.NOT_FOUND),
	MESSAGE_NOT_FOUND("M002", "Message not found", HttpStatus.NOT_FOUND),
	NOT_CHAT_PARTICIPANT("M003", "You are not a participant of this chat", HttpStatus.FORBIDDEN),
	CHAT_ROOM_ALREADY_EXISTS("M004", "Chat room already exists", HttpStatus.CONFLICT),
	
	// ==================== Notification (N0XX) ====================
	NOTIFICATION_NOT_FOUND("N001", "Notification not found", HttpStatus.NOT_FOUND),
	DEVICE_TOKEN_INVALID("N002", "Invalid device token", HttpStatus.BAD_REQUEST),
	
	// ==================== Coupon (U0XX) ====================
	COUPON_NOT_FOUND("U001", "Coupon not found", HttpStatus.NOT_FOUND),
	COUPON_EXPIRED("U002", "Coupon has expired", HttpStatus.GONE),
	COUPON_ALREADY_USED("U003", "Coupon has already been used", HttpStatus.CONFLICT),
	COUPON_NOT_APPLICABLE("U004", "Coupon is not applicable", HttpStatus.BAD_REQUEST),
	COUPON_STOCK_EXHAUSTED("U005", "Coupon stock is exhausted", HttpStatus.CONFLICT);
	
	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
