package com.study.api_gateway.api.reservationManage.dto.response;

import com.study.api_gateway.api.reservationManage.dto.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * YeYakManage 서버 예약 상세 조회 응답용 내부 DTO
 * 서버에서 placeId, roomId만 반환하고, API Gateway에서 enrichment하여 최종 응답 생성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalReservationDetailResponse {
	
	private Long reservationId;
	private Long placeId;
	private Long roomId;
	private Long userId;
	private ReservationStatus status;
	private LocalDate reservationDate;
	private List<String> startTimes;
	private Long totalPrice;
	private Long reservationTimePrice;
	private Boolean isBlackUser;
	private String reserverName;
	private String reserverPhone;
	private List<SelectedProduct> selectedProducts;
	private Map<String, String> additionalInfo;
	private LocalDateTime approvedAt;
	private Long approvedBy;
	private LocalDateTime rejectedAt;
	private String rejectedReason;
	private Long rejectedBy;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SelectedProduct {
		private String productId;
		private String productName;
		private Integer quantity;
		private Long unitPrice;
		private Long subtotal;
	}
}
