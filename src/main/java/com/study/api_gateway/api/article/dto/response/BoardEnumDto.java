package com.study.api_gateway.api.article.dto.response;

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
@Schema(name = "BoardEnumDto", description = "게시판 정보")
public class BoardEnumDto {
	@Schema(description = "게시판 ID", example = "1")
	private Long boardId;
	
	@Schema(description = "게시판 이름", example = "자유게시판")
	private String boardName;
	
	@Schema(description = "게시판 설명", example = "자유롭게 이야기하는 게시판")
	private String description;
}
