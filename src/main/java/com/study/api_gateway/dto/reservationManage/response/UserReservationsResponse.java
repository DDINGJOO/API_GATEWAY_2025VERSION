package com.study.api_gateway.dto.reservationManage.response;

import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "사용자별 예약 목록 응답 (커서 페이징)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReservationsResponse {

	@Schema(description = "예약 항목 목록")
	private List<UserReservationItem> content;
	
	@Schema(description = "커서 정보")
	private CursorInfo cursor;

	@Schema(description = "페이지 크기", example = "20")
	private Integer size;
	
	@Schema(description = "커서 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CursorInfo {
		@Schema(description = "다음 페이지 커서 (Base64 인코딩)")
		private String next;
		
		@Schema(description = "다음 페이지 존재 여부")
		private Boolean hasNext;
	}
	
	@Schema(description = "사용자 예약 항목")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserReservationItem {
		@Schema(description = "예약 ID", example = "258146287942930432")
		private Long reservationId;
		
		@Schema(description = "장소 정보")
		private PlaceInfo placeInfo;
		
		@Schema(description = "방 정보")
		private RoomInfo roomInfo;

		@Schema(description = "예약 날짜", example = "2025-01-15")
		private LocalDate reservationDate;
		
		@Schema(description = "예약 시작 시간 목록", example = "[\"09:00\", \"10:00\"]")
		private List<String> startTimes;
		
		@Schema(description = "예약 상태", example = "PENDING")
		private ReservationStatus status;
		
		@Schema(description = "총 가격 (원)", example = "10000")
		private Long totalPrice;
		
		@Schema(description = "예약자 이름", example = "김영택")
		private String reserverName;
		
		@Schema(description = "예약자 전화번호", example = "01025801478")
		private String reserverPhone;
	}
	
	@Schema(description = "장소 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlaceInfo {
		@Schema(description = "장소 ID", example = "100")
		private Integer placeId;
		
		@Schema(description = "장소 이름", example = "스터디카페 강남점")
		private String placeName;
	}
	
	@Schema(description = "방 정보")
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RoomInfo {
		@Schema(description = "방 ID", example = "10")
		private Integer roomId;
		
		@Schema(description = "방 이름", example = "A룸")
		private String roomName;
		
		@Schema(description = "방 이미지 URL 목록")
		private List<String> imageUrls;
		
		@Schema(description = "시간 슬롯", example = "09:00-12:00")
		private String timeSlot;
	}
}
