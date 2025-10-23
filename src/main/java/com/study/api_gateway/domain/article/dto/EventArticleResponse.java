package com.study.api_gateway.domain.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EventArticleResponse", description = "이벤트 게시글 응답",
		example = "{\n  \"articleId\": \"evt_001\",\n  \"title\": \"밴드 페스티벌\",\n  \"content\": \"연말 밴드 페스티벌이 열립니다.\",\n  \"writerId\": \"user_123\",\n  \"board\": { \"3\": \"EVENT\" },\n  \"LastestUpdateId\": \"2025-01-10T12:34:56\",\n  \"imageUrls\": { \"img_1\": \"https://cdn.example.com/images/img_1.png\" },\n  \"keywords\": { \"10\": \"MUSIC\" },\n  \"eventStartDate\": \"2025-12-24T18:00:00\",\n  \"eventEndDate\": \"2025-12-26T23:00:00\"\n}")
public class EventArticleResponse extends ArticleResponse {
	@Schema(description = "이벤트 시작 날짜", example = "2025-12-24T18:00:00")
	private LocalDateTime eventStartDate;
	
	@Schema(description = "이벤트 종료 날짜", example = "2025-12-26T23:00:00")
	private LocalDateTime eventEndDate;
	
	@Builder(builderMethodName = "eventArticleBuilder")
	public EventArticleResponse(String articleId, String title, String content, String writerId, Map<Long, String> board,
	                            LocalDateTime LastestUpdateId, Map<String, String> imageUrls, Map<Long, String> keywords,
	                            LocalDateTime eventStartDate, LocalDateTime eventEndDate) {
		super(articleId, title, content, writerId, board, LastestUpdateId, imageUrls, keywords);
		this.eventStartDate = eventStartDate;
		this.eventEndDate = eventEndDate;
	}
}
