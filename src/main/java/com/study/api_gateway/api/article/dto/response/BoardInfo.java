package com.study.api_gateway.api.article.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "BoardInfo", description = "게시판 정보")
public class BoardInfo {
	
	@Schema(description = "게시판 ID", example = "1")
	private Long boardId;
	
	@Schema(description = "게시판 이름", example = "자유게시판")
	private String boardName;
}
