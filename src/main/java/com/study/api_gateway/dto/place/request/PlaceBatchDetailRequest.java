package com.study.api_gateway.dto.place.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Place 배치 상세 조회 요청 DTO
 * 여러 Place ID를 한 번에 조회하기 위한 요청
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceBatchDetailRequest {

    /**
     * 조회할 Place ID 목록
     * - 최소 1개, 최대 50개
     * - null 값은 자동 필터링
     * - 중복 값은 자동 제거
     */
    private List<Long> placeIds;
}