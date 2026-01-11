package com.study.api_gateway.api.reservationManage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "일간/주간/월간 예약 목록 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReservationResponse {
	
	@Schema(description = "장소 목록")
	private List<PlaceReservation> places;
	
	@Schema(description = "총 예약 건수")
	private Integer totalCount;
	
	@Schema(description = "조회 기간 타입", example = "DAILY")
	private String period;
	
	@Schema(description = "장소별 예약 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlaceReservation {
		@Schema(description = "장소 ID", example = "100")
		private Long placeId;
		
		@Schema(description = "방 목록")
		private List<RoomReservation> rooms;
	}
	
	@Schema(description = "방별 예약 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RoomReservation {
		@Schema(description = "방 ID", example = "10")
		private Long roomId;
		
		@Schema(description = "예약 목록")
		private List<ReservationItem> reservations;
	}
	
	@Schema(description = "예약 항목")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ReservationItem {
		@Schema(description = "예약 ID", example = "123456")
		private Long reservationId;
		
		@Schema(description = "예약자 이름", example = "홍길동")
		private String reserverName;
		
		@Schema(description = "시작 시간 목록", example = "[\"11:00\", \"12:00\", \"13:00\"]")
		private List<String> startTimes;
		
		@Schema(description = "예약 날짜", example = "2025-01-17")
		private String reservationDate;
		
		@Schema(description = "예약 상태", example = "CONFIRMED")
		private String status;
		
		@Schema(description = "승인 필요 여부", example = "false")
		private Boolean needsApproval;
		
		@Schema(description = "블랙리스트 여부", example = "false")
		private Boolean isBlacklisted;
	}
}
