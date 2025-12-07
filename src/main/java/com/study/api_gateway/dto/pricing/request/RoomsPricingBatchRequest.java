package com.study.api_gateway.dto.pricing.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Room ID 리스트 기반 가격 정책 배치 조회 요청
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomsPricingBatchRequest {

    /**
     * 조회할 Room ID 리스트
     * - 최소 1개 이상
     * - 각 ID는 양수여야 함
     */
    private List<Long> roomIds;

    /**
     * 시간대별 가격 조회 날짜 (선택사항)
     * - 형식: yyyy-MM-dd
     * - null인 경우 기본 가격만 조회
     */
    private LocalDate date;
}