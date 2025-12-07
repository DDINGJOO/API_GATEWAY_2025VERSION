package com.study.api_gateway.dto.coupon.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponReserveRequest {
    private String reservationId;
    private Long userId;
    private Long couponId;
    private Integer orderAmount;
}