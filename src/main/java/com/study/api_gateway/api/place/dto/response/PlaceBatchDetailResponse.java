package com.study.api_gateway.api.place.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Place 배치 상세 조회 응답 DTO
 * 성공한 조회 결과와 실패한 ID를 분리하여 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceBatchDetailResponse {
	
	/**
	 * 조회에 성공한 공간 정보 목록
	 */
	private List<PlaceInfoResponse> results;
	
	/**
	 * 조회에 실패한 placeId 목록
	 * 빈 배열인 경우 필드 자체가 제외됨
	 */
	private List<Long> failed;
}
