package com.study.api_gateway.dto.Article.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "KeywordEnumDto", description = "키워드 정보")
public class KeywordEnumDto {
	@Schema(description = "키워드 ID", example = "1")
	private Long keywordId;
	
	@Schema(description = "키워드 이름", example = "Java")
	private String keywordName;
	
	@Schema(description = "공통 키워드 여부", example = "true")
	private Boolean isCommon;
	
	@Schema(description = "보드 전용 키워드의 게시판 ID (공통 키워드는 null)", example = "null")
	private Long boardId;
	
	@Schema(description = "보드 전용 키워드의 게시판 이름 (공통 키워드는 null)", example = "null")
	private String boardName;
}
