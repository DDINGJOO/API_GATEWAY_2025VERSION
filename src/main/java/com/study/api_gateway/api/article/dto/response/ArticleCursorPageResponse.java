package com.study.api_gateway.api.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ArticleCursorPageResponse", description = "게시글 커서 페이지 응답",
		example = "{\n  \"items\": [\n    {\n      \"articleId\": \"art_001\",\n      \"title\": \"첫 게시글\",\n      \"content\": \"본문...\",\n      \"writerId\": \"user_123\"\n    }\n  ],\n  \"nextCursorUpdatedAt\": \"2025-01-10T12:35:00\",\n  \"nextCursorId\": \"art_050\",\n  \"hasNext\": true,\n  \"size\": 10\n}")
public class ArticleCursorPageResponse {
	@Schema(description = "게시글 목록")
	private List<ArticleResponse> items;
	// 다음 페이지 요청 시 사용할 수정일시 (updated_at)
	@Schema(description = "다음 페이지 조회용 커서 시각", example = "2025-01-10T12:35:00")
	private LocalDateTime nextCursorUpdatedAt;
	@Schema(description = "다음 페이지 조회용 커서 ID", example = "art_050")
	private String nextCursorId;
	@Schema(description = "다음 페이지 존재 여부", example = "true")
	private boolean hasNext;
	@Schema(description = "페이지 사이즈", example = "10")
	private int size;
}
