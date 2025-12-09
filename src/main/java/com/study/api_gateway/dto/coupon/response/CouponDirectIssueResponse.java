package com.study.api_gateway.dto.coupon.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDirectIssueResponse {
	private Integer totalRequested;
	private Integer successCount;
	private Integer failureCount;
	private List<IssueResult> results;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class IssueResult {
		private Long userId;
		private Long couponId;
		private String status;
	}
}
