package com.study.api_gateway.dto.reservation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 예약 시간대별 가격 상세 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTimePriceDetail {
	private List<String> startTimes;
	private BigDecimal totalReservationTimePrice;
}