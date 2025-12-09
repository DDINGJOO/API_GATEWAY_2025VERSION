package com.study.api_gateway.dto.Article.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 게시글 생성 요청
 * NOTE: ArticleCreateRequest에 이미 eventStartDate/eventEndDate가 포함되어 있어 별도 클래스가 필요 없지만,
 * 기존 코드 호환성을 위해 유지합니다.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "EventArticleCreateRequest", description = "이벤트 게시글 생성 요청 바디 (ArticleCreateRequest와 동일, 하위 호환용)",
		example = "{\n  \"title\": \"공연 함께 하실 분\",\n  \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n  \"writerId\": \"user_123\",\n  \"boardIds\": 1,\n  \"keywordIds\": [10, 12],\n  \"imageUrls\": [\"img_1\", \"img_2\"],\n  \"eventStartDate\": \"2025-10-15T09:00:00\",\n  \"eventEndDate\": \"2025-10-20T18:00:00\"\n}")
public class EventArticleCreateRequest extends ArticleCreateRequest {
	
	@Builder(builderMethodName = "eventBuilder")
	public EventArticleCreateRequest(String title, String content, String writerId,
	                                 Long boardIds, List<Long> keywordIds, List<String> imageUrls,
	                                 LocalDateTime eventStartDate, LocalDateTime eventEndDate) {
		super(title, content, writerId, boardIds, keywordIds, imageUrls, eventStartDate, eventEndDate);
	}
}
