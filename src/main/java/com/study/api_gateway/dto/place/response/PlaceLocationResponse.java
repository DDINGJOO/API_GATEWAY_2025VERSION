package com.study.api_gateway.dto.place.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 위치 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLocationResponse {
	
	private AddressResponse address;
	private Double latitude;
	private Double longitude;
	private String locationGuide;
}
