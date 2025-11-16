package com.study.api_gateway.dto.Article.request;

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
@Schema(name = "ArticleCreateRequest", description = "게시글 생성/수정 요청 바디",
		example = "{\n  \"title\": \"공연 함께 하실 분\",\n  \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n  \"writerId\": \"user_123\",\n  \"boardIds\": 1,\n  \"keywordIds\": [10, 12],\n  \"imageUrls\": [\"img_1\", \"img_2\"]\n}")
public class ArticleCreateRequest {
	@Schema(description = "제목 (최대 100자)", example = "공연 함께 하실 분", required = true)
	private String title;
	
	@Schema(description = "내용", example = "같이 즐겁게 공연하실 분을 찾습니다.", required = true)
	private String content;
	
	@Schema(description = "작성자 ID", example = "user_123", required = true)
	private String writerId;
	
	@Schema(description = "게시판 ID", example = "1", required = true)
	private Long boardIds;

	@Schema(description = "키워드 ID 목록", example = "[10, 12]")
	private List<Long> keywordIds;
	
	@Schema(description = "이미지 ID 목록 (Gateway에서 이미지 확정 처리용)", example = "[\"img_1\", \"img_2\"]")
	private List<String> imageIds;
	
	@Schema(description = "이벤트 시작일 (이벤트 게시글만)", example = "2025-01-01T00:00:00")
	private LocalDateTime eventStartDate;
	
	@Schema(description = "이벤트 종료일 (이벤트 게시글만)", example = "2025-12-31T23:59:59")
	private LocalDateTime eventEndDate;
}
