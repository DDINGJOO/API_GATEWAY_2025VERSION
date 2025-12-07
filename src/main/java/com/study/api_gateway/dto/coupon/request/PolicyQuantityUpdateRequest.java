package com.study.api_gateway.dto.coupon.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyQuantityUpdateRequest {
    private Integer newMaxIssueCount;
    private String modifiedBy;
    private String reason;
}