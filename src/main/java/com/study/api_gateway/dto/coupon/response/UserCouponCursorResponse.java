package com.study.api_gateway.dto.coupon.response;

import com.study.api_gateway.dto.coupon.enums.CouponStatus;
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
public class UserCouponCursorResponse {
    private List<CouponItem> coupons;
    private Long nextCursor;
    private Boolean hasMore;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouponItem {
        private Long couponId;
        private String couponName;
        private DiscountType discountType;
        private Integer discountValue;
        private CouponStatus status;
        private LocalDateTime expiryDate;
        private LocalDateTime issuedAt;
    }
}