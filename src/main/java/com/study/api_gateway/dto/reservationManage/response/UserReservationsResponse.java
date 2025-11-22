package com.study.api_gateway.dto.reservationManage.response;

import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "사용자별 예약 목록 응답 (커서 페이징)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReservationsResponse {
	
	@Schema(description = "예약 항목 목록")
	private List<UserReservationItem> items;
	
	@Schema(description = "다음 페이지 커서 (Base64 인코딩)", example = "eyJyZXNlcnZhdGlvbkRhdGUiOiIyMDI1LTAxLTE0IiwicmVzZXJ2YXRpb25JZCI6MTIzNDU1fQ==")
	private String nextCursor;
	
	@Schema(description = "다음 페이지 존재 여부", example = "true")
	private Boolean hasNext;
	
	@Schema(description = "페이지 크기", example = "20")
	private Integer size;
	
	@Schema(description = "사용자 예약 항목")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserReservationItem {
		@Schema(description = "예약 ID", example = "123456")
		private Long reservationId;
		
		@Schema(description = "장소 ID", example = "100")
		private Long placeId;
		
		@Schema(description = "방 ID", example = "10")
		private Long roomId;
		
		@Schema(description = "예약 상태", example = "CONFIRMED")
		private ReservationStatus status;
		
		@Schema(description = "총 가격 (원)", example = "72000")
		private Long totalPrice;
		
		@Schema(description = "예약 시간 가격 (원)", example = "60000")
		private Long reservationTimePrice;
		
		@Schema(description = "보증금 (원)", example = "10000")
		private Long depositPrice;
		
		@Schema(description = "예약 날짜", example = "2025-01-15")
		private LocalDate reservationDate;
		
		@Schema(description = "예약자 이름", example = "홍길동")
		private String reserverName;
		
		@Schema(description = "예약자 전화번호", example = "010-1234-5678")
		private String reserverPhone;
		
		@Schema(description = "승인 시각", example = "2025-01-15T10:30:00")
		private LocalDateTime approvedAt;
		
		@Schema(description = "승인자", example = "0")
		private Long approvedBy;
		
		@Schema(description = "생성 시각", example = "2025-01-15T10:00:00")
		private LocalDateTime createdAt;
	}
}
