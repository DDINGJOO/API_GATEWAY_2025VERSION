package com.study.api_gateway.domain.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "EventArticleCreateRequest", description = "이벤트 게시글 생성 요청 바디",
		example = "{\n  \"title\": \"공연 함께 하실 분\",\n  \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n  \"writerId\": \"user_123\",\n  \"imageUrls\": [\"img_1\", \"img_2\"],\n  \"keywords\": [10, 12],\n  \"board\": 1,\n  \"eventStartDate\": \"2025-10-15T09:00:00\",\n  \"eventEndDate\": \"2025-10-20T18:00:00\"\n}")
public class EventArticleCreateRequest extends ArticleCreateRequest {
	@Schema(description = "이벤트 시작 날짜 및 시간", example = "2025-10-15T09:00:00", required = true)
	private LocalDateTime eventStartDate;
	
	@Schema(description = "이벤트 종료 날짜 및 시간", example = "2025-10-20T18:00:00", required = true)
	private LocalDateTime eventEndDate;
	
	@Builder(builderMethodName = "eventBuilder")
	public EventArticleCreateRequest(String title, String content, String writerId,
	                                 java.util.List<String> imageUrls, java.util.List<?> keywords,
	                                 Object board, LocalDateTime eventStartDate, LocalDateTime eventEndDate) {
		super(title, content, writerId, imageUrls, keywords, board);
		this.eventStartDate = eventStartDate;
		this.eventEndDate = eventEndDate;
	}
}
