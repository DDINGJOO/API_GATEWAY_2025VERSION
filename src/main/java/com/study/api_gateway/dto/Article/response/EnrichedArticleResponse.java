package com.study.api_gateway.dto.Article.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로필 정보가 포함된 게시글 응답 DTO
 * <p>
 * API Gateway에서 Article 도메인 응답에 프로필 정보를 추가한 클라이언트 응답용 DTO입니다.
 * Feed 조회, 게시글 목록 조회 등 프로필 정보가 필요한 모든 곳에서 사용됩니다.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "EnrichedArticleResponse", description = "프로필 정보가 포함된 게시글 응답",
		example = "{\n  \"articleId\": \"ART_001\",\n  \"title\": \"게시글 제목\",\n  \"writerId\": \"user123\",\n  \"writerName\": \"홍길동\",\n  \"writerProfileImage\": \"https://cdn.example.com/profile.jpg\",\n  \"boardId\": 1,\n  \"boardName\": \"자유게시판\",\n  \"articleType\": \"REGULAR\",\n  \"status\": \"ACTIVE\",\n  \"viewCount\": 10,\n  \"firstImageUrl\": \"https://cdn.example.com/image.jpg\",\n  \"createdAt\": \"2025-10-25T10:30:00\",\n  \"updatedAt\": \"2025-10-25T10:30:00\"\n}")
public class EnrichedArticleResponse {
	
	// ========== Article 도메인 정보 ==========
	
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
	
	@Schema(description = "첫 번째 이미지 URL", example = "https://cdn.example.com/image.jpg")
	private String firstImageUrl;
	
	@Schema(description = "생성일시", example = "2025-10-25T10:30:00")
	private LocalDateTime createdAt;
	
	@Schema(description = "수정일시", example = "2025-10-25T10:30:00")
	private LocalDateTime updatedAt;
	
	// ========== Enrichment 정보 (API Gateway에서 추가) ==========
	
	@Schema(description = "작성자 이름", example = "홍길동")
	private String writerName;
	
	@Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.example.com/profile.jpg")
	private String writerProfileImage;
	
	@Schema(description = "댓글 수", example = "5")
	private Integer commentCount;
	
	@Schema(description = "좋아요 수", example = "10")
	private Integer likeCount;
	
	/**
	 * ArticleSimpleResponse로부터 변환 (프로필 정보 없이)
	 */
	public static EnrichedArticleResponse from(ArticleSimpleResponse article) {
		if (article == null) {
			return null;
		}
		
		return EnrichedArticleResponse.builder()
				.articleId(article.getArticleId())
				.title(article.getTitle())
				.writerId(article.getWriterId())
				.boardId(article.getBoardId())
				.boardName(article.getBoardName())
				.articleType(article.getArticleType())
				.status(article.getStatus())
				.viewCount(article.getViewCount())
				.firstImageUrl(article.getFirstImageUrl())
				.createdAt(article.getCreatedAt())
				.updatedAt(article.getUpdatedAt())
				.build();
	}
	
	/**
	 * 프로필 정보 추가
	 */
	public EnrichedArticleResponse withProfile(String writerName, String writerProfileImage) {
		this.writerName = writerName;
		this.writerProfileImage = writerProfileImage;
		return this;
	}
	
	/**
	 * 댓글 수 및 좋아요 수 추가
	 */
	public EnrichedArticleResponse withCounts(Integer commentCount, Integer likeCount) {
		this.commentCount = commentCount;
		this.likeCount = likeCount;
		return this;
	}
}
