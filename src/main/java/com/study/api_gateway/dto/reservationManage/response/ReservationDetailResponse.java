package com.study.api_gateway.dto.reservationManage.response;

import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "예약 상세 정보 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {
	
	@Schema(description = "예약 ID", example = "123456")
	private Long reservationId;
	
	@Schema(description = "사용자 ID", example = "1001")
	private Long userId;
	
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
	
	@Schema(description = "리뷰 ID", example = "1")
	private Long reviewId;
	
	@Schema(description = "예약 날짜", example = "2025-01-15")
	private LocalDate reservationDate;
	
	@Schema(description = "예약자 이름", example = "홍길동")
	private String reserverName;
	
	@Schema(description = "예약자 전화번호", example = "010-1234-5678")
	private String reserverPhone;
	
	@Schema(description = "승인 시각", example = "2025-01-15T10:30:00")
	private LocalDateTime approvedAt;
	
	@Schema(description = "승인자 (0: 시스템, 양수: 운영자 ID)", example = "0")
	private Long approvedBy;
	
	@Schema(description = "생성 시각", example = "2025-01-15T10:00:00")
	private LocalDateTime createdAt;
	
	@Schema(description = "수정 시각", example = "2025-01-15T10:30:00")
	private LocalDateTime updatedAt;
}
