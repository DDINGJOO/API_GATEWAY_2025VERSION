package com.study.api_gateway.dto.reservationManage.response;

import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * YeYakManage 서버 응답용 내부 DTO
 * 서버에서 placeId, roomId만 반환하고, API Gateway에서 enrichment하여 최종 응답 생성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserReservationsResponse {
	
	private List<InternalUserReservationItem> content;
	private CursorInfo cursor;
	private Integer size;
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CursorInfo {
		private String next;
		private Boolean hasNext;
	}
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class InternalUserReservationItem {
		private Long reservationId;
		private Long placeId;
		private Long roomId;
		private LocalDate reservationDate;
		private List<String> startTimes;
		private ReservationStatus status;
		private Long totalPrice;
		private String reserverName;
		private String reserverPhone;
	}
}
