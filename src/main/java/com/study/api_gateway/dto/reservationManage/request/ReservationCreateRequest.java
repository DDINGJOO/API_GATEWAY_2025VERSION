package com.study.api_gateway.dto.reservationManage.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "예약 생성 요청 (예약자 정보 업데이트)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequest {
	
	@Schema(description = "예약 ID", example = "123456", required = true)
	private Long reservationId;
	
	@Schema(description = "예약자 이름", example = "홍길동", required = true)
	private String reserverName;
	
	@Schema(description = "예약자 전화번호", example = "010-1234-5678", required = true)
	private String reserverPhone;
}
