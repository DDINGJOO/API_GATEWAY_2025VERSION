package com.study.api_gateway.dto.coupon.response;

import com.study.api_gateway.dto.coupon.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponApplyResponse {
	private String couponId;
	private String couponName;
	private DiscountType discountType;
	private Integer discountValue;
	private Integer maxDiscountAmount;
}
