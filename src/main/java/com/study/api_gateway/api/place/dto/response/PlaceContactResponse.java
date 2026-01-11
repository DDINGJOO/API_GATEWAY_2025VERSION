package com.study.api_gateway.api.place.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장소 연락처 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceContactResponse {
	
	private String contact;
	private String email;
	private List<String> websites;
	private List<String> socialLinks;
}
