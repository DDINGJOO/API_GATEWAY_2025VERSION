package com.study.api_gateway.api.coupon.dto.response;

import com.study.api_gateway.api.coupon.dto.enums.CouponStatus;
import com.study.api_gateway.api.coupon.dto.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponListResponse {
	private List<UserCouponItem> content;
	private Long totalElements;
	private Integer totalPages;
	private Integer number;
	private Integer size;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserCouponItem {
		private Long couponId;
		private String couponName;
		private DiscountType discountType;
		private Integer discountValue;
		private CouponStatus status;
		private LocalDateTime expiryDate;
		private LocalDateTime issuedAt;
	}
}
