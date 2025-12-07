package com.study.api_gateway.dto.pricing.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Room별 가격 정보
 * 배치 조회 시 각 Room의 가격 정책 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomPricingInfo {

    /**
     * Room 식별자
     */
    private Long roomId;

    /**
     * 시간 단위 (HOUR=60분, HALFHOUR=30분)
     */
    private String timeSlot;

    /**
     * 기본 가격 (시간대별 가격이 없을 때 적용)
     */
    private BigDecimal defaultPrice;

    /**
     * 시간대별 가격 맵
     * 키: "HH:mm" 형식 (24시간)
     * 값: 해당 시간대 가격
     * date 파라미터 제공 시에만 포함됨
     */
    private Map<String, BigDecimal> timeSlotPrices;
}