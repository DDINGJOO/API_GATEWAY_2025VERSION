package com.study.api_gateway.dto.coupon.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponReservationResponse {
    private String reservationId;
    private Long couponId;
    private Boolean success;
    private String message;
    private LocalDateTime reservedAt;
}