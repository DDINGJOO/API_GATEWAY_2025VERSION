package com.study.api_gateway.dto.reservation.response;

import com.study.api_gateway.dto.reservation.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 예약 가격 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPricingResponse {
	private Long reservationId;
	private Long roomId;
	private ReservationStatus status;
	private BigDecimal totalPrice;
	private LocalDateTime calculatedAt;
}
