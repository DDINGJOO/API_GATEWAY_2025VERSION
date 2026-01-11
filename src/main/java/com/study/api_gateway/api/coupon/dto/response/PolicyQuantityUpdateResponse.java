package com.study.api_gateway.api.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyQuantityUpdateResponse {
	private Long couponPolicyId;
	private Integer previousMaxIssueCount;
	private Integer newMaxIssueCount;
	private Integer currentIssuedCount;
	private Integer remainingCount;
	private Boolean success;
	private String message;
}
