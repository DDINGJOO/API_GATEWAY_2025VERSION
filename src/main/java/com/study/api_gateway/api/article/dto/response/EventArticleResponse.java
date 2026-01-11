package com.study.api_gateway.api.article.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 게시글 응답
 * NOTE: ArticleResponse에 이미 eventStartDate/eventEndDate가 포함되어 있어 별도 클래스가 필요 없지만,
 * 기존 코드 호환성을 위해 유지합니다.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "EventArticleResponse", description = "이벤트 게시글 응답 (ArticleResponse와 동일, 하위 호환용)",
		example = "{\n  \"articleId\": \"ART_20251225_001\",\n  \"title\": \"크리스마스 이벤트\",\n  \"content\": \"크리스마스 특별 이벤트입니다.\",\n  \"writerId\": \"admin\",\n  \"board\": { \"boardId\": 3, \"boardName\": \"이벤트\", \"description\": \"이벤트 게시판\" },\n  \"status\": \"ACTIVE\",\n  \"viewCount\": 100,\n  \"firstImageUrl\": null,\n  \"createdAt\": \"2025-12-01T00:00:00\",\n  \"updatedAt\": \"2025-12-01T00:00:00\",\n  \"images\": [],\n  \"keywords\": [],\n  \"eventStartDate\": \"2025-12-24T00:00:00\",\n  \"eventEndDate\": \"2025-12-25T23:59:59\"\n}")
public class EventArticleResponse extends ArticleResponse {
	
	@Builder(builderMethodName = "eventArticleBuilder")
	public EventArticleResponse(
			String articleId,
			String title,
			String content,
			String writerId,
			String writerName,
			String writerProfileImage,
			BoardInfo board,
			String status,
			Integer viewCount,
			String firstImageUrl,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			List<ImageInfo> images,
			List<KeywordInfo> keywords,
			LocalDateTime eventStartDate,
			LocalDateTime eventEndDate) {
		super(articleId, title, content, writerId, writerName, writerProfileImage, board, status, viewCount,
				firstImageUrl, createdAt, updatedAt, images, keywords, eventStartDate, eventEndDate);
	}
}
