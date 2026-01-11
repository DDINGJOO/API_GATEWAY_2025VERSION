package com.study.api_gateway.api.coupon.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 선착순 쿠폰 발급 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class FcfsIssueRequest {
	private Long policyId;
	private Long userId;
}
