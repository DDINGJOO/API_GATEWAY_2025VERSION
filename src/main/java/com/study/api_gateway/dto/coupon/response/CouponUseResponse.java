package com.study.api_gateway.dto.coupon.response;

import com.study.api_gateway.dto.coupon.enums.CouponStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUseResponse {
	private Long couponId;
	private CouponStatus status;
	private LocalDateTime usedAt;
	private String orderId;
	private Integer discountAmount;
}
