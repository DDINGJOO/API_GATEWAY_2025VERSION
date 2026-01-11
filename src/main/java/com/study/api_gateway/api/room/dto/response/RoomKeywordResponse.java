package com.study.api_gateway.api.room.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 룸 키워드 정보 응답 DTO
 */
@Schema(description = "키워드 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomKeywordResponse {
	
	@Schema(description = "키워드 ID", example = "1")
	private Long keywordId;
	
	@Schema(description = "키워드", example = "조용한")
	private String keyword;
}
