package com.study.api_gateway.api.coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDirectIssueRequest {
	private Long couponPolicyId;
	private List<Long> userIds;
	private String issuedBy;
}
