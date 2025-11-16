package com.study.api_gateway.dto.Article.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "ArticleSimpleResponse", description = "게시글 간단 응답 (대량 조회용)",
		example = "{\n  \"articleId\": \"ART_001\",\n  \"title\": \"게시글 제목\",\n  \"writerId\": \"user123\",\n  \"boardId\": 1,\n  \"boardName\": \"자유게시판\",\n  \"articleType\": \"REGULAR\",\n  \"status\": \"ACTIVE\",\n  \"viewCount\": 10,\n  \"firstImageUrl\": \"https://cdn.teambind.com/images/img1.webp\",\n  \"createdAt\": \"2025-10-25T10:30:00\",\n  \"updatedAt\": \"2025-10-25T10:30:00\"\n}")
public class ArticleSimpleResponse {
	@Schema(description = "게시글 ID", example = "ART_001")
	private String articleId;
	
	@Schema(description = "제목", example = "게시글 제목")
	private String title;
	
	@Schema(description = "작성자 ID", example = "user123")
	private String writerId;
	
	@Schema(description = "게시판 ID", example = "1")
	private Long boardId;
	
	@Schema(description = "게시판 이름", example = "자유게시판")
	private String boardName;
	
	@Schema(description = "게시글 타입", example = "REGULAR", allowableValues = {"REGULAR", "EVENT", "NOTICE"})
	private String articleType;
	
	@Schema(description = "게시글 상태", example = "ACTIVE")
	private String status;
	
	@Schema(description = "조회수", example = "10")
	private Integer viewCount;
	
	@Schema(description = "첫 번째 이미지 URL", example = "https://cdn.teambind.com/images/img1.webp")
	private String firstImageUrl;
	
	@Schema(description = "생성일시", example = "2025-10-25T10:30:00")
	private LocalDateTime createdAt;
	
	@Schema(description = "수정일시", example = "2025-10-25T10:30:00")
	private LocalDateTime updatedAt;


}
