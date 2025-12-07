package com.study.api_gateway.dto.coupon.response;

import com.study.api_gateway.dto.coupon.enums.DiscountType;
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
public class ExpiringCouponsResponse {
    private List<ExpiringCoupon> coupons;
    private Integer totalCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpiringCoupon {
        private Long couponId;
        private String couponName;
        private DiscountType discountType;
        private Integer discountValue;
        private LocalDateTime expiryDate;
        private Integer daysUntilExpiry;
    }
}