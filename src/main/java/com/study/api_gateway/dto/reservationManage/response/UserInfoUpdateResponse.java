package com.study.api_gateway.dto.reservationManage.response;

import com.study.api_gateway.dto.reservationManage.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "예약 사용자 정보 업데이트 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoUpdateResponse {

    @Schema(description = "예약 ID", example = "123")
    private Long reservationId;

    @Schema(description = "예약자 이름", example = "홍길동")
    private String reserverName;

    @Schema(description = "예약자 전화번호", example = "01012345678")
    private String reserverPhone;

    @Schema(description = "예약 상태", example = "PENDING")
    private ReservationStatus status;

    @Schema(description = "생성 시간", example = "2025-12-04T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "추가 정보")
    private Map<String, String> additionalInfo;
}