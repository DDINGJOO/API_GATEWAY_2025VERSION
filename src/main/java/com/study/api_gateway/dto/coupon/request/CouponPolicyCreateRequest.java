package com.study.api_gateway.dto.coupon.request;

import com.study.api_gateway.dto.coupon.enums.DiscountType;
import com.study.api_gateway.dto.coupon.enums.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponPolicyCreateRequest {
	private String couponName;
	private String couponCode;
	private DiscountType discountType;
	private Integer discountValue;
	private Integer minimumOrderAmount;
	private Integer maxDiscountAmount;
	private IssueType issueType;
	private Integer maxIssueCount;
	private Integer maxIssuePerUser;
	private Integer validDays;
	private LocalDateTime issueStartDate;
	private LocalDateTime issueEndDate;
}
