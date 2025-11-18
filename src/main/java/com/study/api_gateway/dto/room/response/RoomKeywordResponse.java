package com.study.api_gateway.dto.room.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 룸 키워드 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomKeywordResponse {
	
	private Long id;
	private String name;
	private String description;
}
