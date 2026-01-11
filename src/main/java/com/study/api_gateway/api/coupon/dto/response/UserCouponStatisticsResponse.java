package com.study.api_gateway.api.coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponStatisticsResponse {
	private Integer totalCoupons;
	private Integer availableCoupons;
	private Integer usedCoupons;
	private Integer expiredCoupons;
	private Integer totalSavedAmount;
}
