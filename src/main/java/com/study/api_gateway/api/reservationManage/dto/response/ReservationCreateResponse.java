package com.study.api_gateway.api.reservationManage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "예약 생성 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateResponse {
	
	@Schema(description = "예약 ID", example = "123456")
	private Long reservationId;
	
	@Schema(description = "메시지", example = "예약자 정보가 업데이트되었습니다.")
	private String message;
}
