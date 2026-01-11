package com.study.api_gateway.api.place.dto.response;

import com.study.api_gateway.api.place.dto.enums.ParkingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 주차 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceParkingResponse {
	
	private Boolean available;
	private ParkingType parkingType;
	private String description;
}
