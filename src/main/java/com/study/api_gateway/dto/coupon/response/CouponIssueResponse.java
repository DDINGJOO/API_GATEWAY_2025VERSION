package com.study.api_gateway.dto.coupon.response;

import com.study.api_gateway.dto.coupon.enums.CouponStatus;
import com.study.api_gateway.dto.coupon.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponIssueResponse {
	private Long couponId;
	private String couponName;
	private DiscountType discountType;
	private Integer discountValue;
	private CouponStatus status;
	private LocalDateTime expiryDate;
	private LocalDateTime issuedAt;
}
