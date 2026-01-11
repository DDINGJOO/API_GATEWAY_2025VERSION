package com.study.api_gateway.api.reservationManage.dto.response;

import com.study.api_gateway.api.reservationManage.dto.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
	
	@Schema(description = "장소 정보")
	private PlaceInfo placeInfo;
	
	@Schema(description = "방 정보")
	private RoomInfo roomInfo;

	@Schema(description = "예약 상태", example = "CONFIRMED")
	private ReservationStatus status;
	
	@Schema(description = "예약 날짜", example = "2025-12-20")
	private LocalDate reservationDate;
	
	@Schema(description = "예약 시작 시간 목록", example = "[\"10:00\", \"11:00\", \"12:00\"]")
	private List<String> startTimes;
	
	@Schema(description = "총 가격 (원)", example = "75000")
	private Long totalPrice;
	
	@Schema(description = "예약 시간 가격 (원)", example = "50000")
	private Long reservationTimePrice;
	
	@Schema(description = "블랙리스트 여부", example = "false")
	private Boolean isBlackUser;

	@Schema(description = "예약자 이름", example = "홍길동")
	private String reserverName;
	
	@Schema(description = "예약자 전화번호", example = "01012345678")
	private String reserverPhone;
	
	@Schema(description = "선택한 상품 목록")
	private List<SelectedProduct> selectedProducts;
	
	@Schema(description = "추가 정보")
	private Map<String, String> additionalInfo;
	
	@Schema(description = "승인 시각", example = "2025-12-16T14:30:00")
	private LocalDateTime approvedAt;
	
	@Schema(description = "승인자 ID", example = "50")
	private Long approvedBy;
	
	@Schema(description = "거절 시각")
	private LocalDateTime rejectedAt;
	
	@Schema(description = "거절 사유")
	private String rejectedReason;
	
	@Schema(description = "거절자 ID")
	private Long rejectedBy;
	
	@Schema(description = "생성 시각", example = "2025-12-15T10:00:00")
	private LocalDateTime createdAt;
	
	@Schema(description = "수정 시각", example = "2025-12-16T14:30:00")
	private LocalDateTime updatedAt;
	
	@Schema(description = "장소 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlaceInfo {
		@Schema(description = "장소 ID", example = "100")
		private Long placeId;
		
		@Schema(description = "장소 이름", example = "홍대 뮤직스튜디오")
		private String placeName;
		
		@Schema(description = "전체 주소", example = "서울 마포구 양화로 153")
		private String fullAddress;
		
		@Schema(description = "위도", example = "37.5556")
		private Double latitude;
		
		@Schema(description = "경도", example = "126.9233")
		private Double longitude;
	}
	
	@Schema(description = "방 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RoomInfo {
		@Schema(description = "방 ID", example = "200")
		private Long roomId;
		
		@Schema(description = "방 이름", example = "A룸")
		private String roomName;
		
		@Schema(description = "방 이미지 URL 목록")
		private List<String> imageUrls;
		
		@Schema(description = "시간 슬롯", example = "HOUR")
		private String timeSlot;
	}
	
	@Schema(description = "선택한 상품")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SelectedProduct {
		@Schema(description = "상품 ID", example = "PROD001")
		private String productId;
		
		@Schema(description = "상품명", example = "음료 세트")
		private String productName;
		
		@Schema(description = "수량", example = "2")
		private Integer quantity;
		
		@Schema(description = "단가", example = "5000")
		private Long unitPrice;
		
		@Schema(description = "소계", example = "10000")
		private Long subtotal;
	}
}
