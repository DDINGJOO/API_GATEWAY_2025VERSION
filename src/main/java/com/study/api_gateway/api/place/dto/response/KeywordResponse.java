package com.study.api_gateway.api.place.dto.response;

import com.study.api_gateway.api.place.dto.enums.KeywordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 키워드 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordResponse {
	
	private Long id;
	private String name;
	private KeywordType type;
	private String description;
	private Integer displayOrder;
}
