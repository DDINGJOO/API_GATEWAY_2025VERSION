package com.study.api_gateway.api.reservationManage.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "예약 사용자 정보 업데이트 요청")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoUpdateRequest {
	
	@Schema(description = "사용자 ID", example = "12345", required = true)
	@NotNull(message = "사용자 ID는 필수입니다")
	private Long userId;
	
	@Schema(description = "예약자 이름", example = "홍길동", required = true)
	@NotBlank(message = "예약자 이름은 필수입니다")
	@Size(min = 2, max = 50, message = "예약자 이름은 2자 이상 50자 이하여야 합니다")
	private String reserverName;
	
	@Schema(description = "예약자 전화번호", example = "01012345678", required = true)
	@NotBlank(message = "예약자 전화번호는 필수입니다")
	@Pattern(regexp = "^[0-9]{11}$", message = "전화번호는 11자리 숫자여야 합니다")
	private String reserverPhone;
	
	@Schema(description = "쿠폰 정보", required = false)
	private CouponInfo couponInfo;
	
	@Schema(description = "추가 정보", required = false)
	private Map<String, String> additionalInfo;
	
	// Gateway에서 사용할 필드들 (API로 전송하지 않음)
	@JsonIgnore
	private Long roomId;
	
	@JsonIgnore
	private Long placeId;
	
	@JsonIgnore
	private Long couponId;  // 쿠폰 적용에 사용할 쿠폰 ID
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CouponInfo {
		@Schema(description = "쿠폰 ID", example = "COUPON-123")
		private String couponId;
		
		@Schema(description = "쿠폰 이름", example = "신규 회원 5000원 할인")
		private String couponName;
		
		@Schema(description = "할인 타입", example = "AMOUNT", allowableValues = {"AMOUNT", "PERCENTAGE"})
		private String discountType;
		
		@Schema(description = "할인 금액 또는 퍼센트", example = "5000")
		private Integer discountValue;
		
		@Schema(description = "최대 할인 금액 (퍼센트 할인일 경우)", example = "10000")
		private Integer maxDiscountAmount;
	}
}
