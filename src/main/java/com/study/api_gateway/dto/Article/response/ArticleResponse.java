package com.study.api_gateway.dto.Article.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "ArticleResponse", description = "게시글 응답",
		example = "{\n  \"articleId\": \"ART_20251025_001\",\n  \"title\": \"첫 게시글\",\n  \"content\": \"안녕하세요 밴더입니다.\",\n  \"writerId\": \"user_123\",\n  \"status\": \"ACTIVE\",\n  \"viewCount\": 15,\n  \"board\": { \"boardId\": 1, \"boardName\": \"자유게시판\", \"description\": \"자유롭게 이야기하는 게시판\" },\n  \"keywords\": [{ \"keywordId\": 1, \"keywordName\": \"Java\", \"isCommon\": true, \"boardId\": null, \"boardName\": null }],\n  \"images\": [{ \"imageId\": \"IMG_001\", \"imageUrl\": \"https://cdn.example.com/img1.webp\", \"sequence\": 1 }],\n  \"firstImageUrl\": \"https://cdn.example.com/img1.webp\",\n  \"createdAt\": \"2025-10-25T10:30:00\",\n  \"updatedAt\": \"2025-10-25T10:30:00\"\n}")
public class ArticleResponse {
	@Schema(description = "게시글 ID", example = "ART_20251025_001")
	private String articleId;

	@Schema(description = "제목", example = "첫 게시글")
	private String title;
	
	@Schema(description = "내용", example = "안녕하세요 밴더입니다.")
	private String content;
	
	@Schema(description = "작성자 ID", example = "user_123")
	private String writerId;
	
	@Schema(description = "게시판 정보")
	private BoardInfo board;
	
	@Schema(description = "게시글 상태", example = "ACTIVE")
	private String status;
	
	@Schema(description = "조회수", example = "15")
	private Integer viewCount;
	
	@Schema(description = "첫 번째 이미지 URL", example = "https://cdn.teambind.com/images/img1.webp")
	private String firstImageUrl;
	
	@Schema(description = "생성일시", example = "2025-10-25T10:30:00")
	private LocalDateTime createdAt;
	
	@Schema(description = "수정일시", example = "2025-10-25T10:30:00")
	private LocalDateTime updatedAt;
	
	@Schema(description = "이미지 목록")
	private List<ImageInfo> images;
	
	@Schema(description = "키워드 목록")
	private List<KeywordInfo> keywords;
	
	// 이벤트 게시글용 필드
	@Schema(description = "이벤트 시작일 (이벤트 게시글만)", example = "2025-01-01T00:00:00")
	private LocalDateTime eventStartDate;
	
	@Schema(description = "이벤트 종료일 (이벤트 게시글만)", example = "2025-12-31T23:59:59")
	private LocalDateTime eventEndDate;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class BoardInfo {
		@Schema(description = "게시판 ID", example = "1")
		private Long boardId;
		
		@Schema(description = "게시판 이름", example = "자유게시판")
		private String boardName;
		
		@Schema(description = "게시판 설명", example = "자유롭게 이야기하는 게시판")
		private String description;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class KeywordInfo {
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
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ImageInfo {
		@Schema(description = "이미지 ID", example = "IMG_001")
		private String imageId;
		
		@Schema(description = "이미지 URL", example = "https://cdn.teambind.com/images/img1.webp")
		private String imageUrl;
		
		@Schema(description = "이미지 순서", example = "1")
		private Integer sequence;
	}
}
