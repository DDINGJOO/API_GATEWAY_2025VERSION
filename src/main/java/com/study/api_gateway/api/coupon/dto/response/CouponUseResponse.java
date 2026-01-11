package com.study.api_gateway.api.coupon.dto.response;

import com.study.api_gateway.api.coupon.dto.enums.CouponStatus;
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
