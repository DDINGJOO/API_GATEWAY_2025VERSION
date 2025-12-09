package com.study.api_gateway.dto.activity.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.study.api_gateway.dto.Article.response.EnrichedArticleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 프로필 정보가 포함된 피드 페이지 응답
 * <p>
 * API Gateway에서 Feed 응답에 Article 정보와 프로필 정보를 추가한 클라이언트 응답용 DTO입니다.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "EnrichedFeedPageResponse", description = "프로필 정보가 포함된 피드 페이지 응답",
		example = "{\\n  \\\"articles\\\": [\\n    {\\n      \\\"articleId\\\": \\\"ART_001\\\",\\n      \\\"title\\\": \\\"게시글 제목\\\",\\n      \\\"writerId\\\": \\\"user123\\\",\\n      \\\"writerName\\\": \\\"홍길동\\\",\\n      \\\"writerProfileImage\\\": \\\"https://cdn.example.com/profile.jpg\\\",\\n      \\\"boardId\\\": 1,\\n      \\\"boardName\\\": \\\"자유게시판\\\",\\n      \\\"viewCount\\\": 10\\n    }\\n  ],\\n  \\\"nextCursor\\\": \\\"ART_001\\\"\\n}")
public class EnrichedFeedPageResponse {
	
	@Schema(description = "프로필 정보가 포함된 게시글 리스트")
	private List<EnrichedArticleResponse> articles;
	
	@Schema(description = "다음 페이지 커서", example = "ART_001")
	private String nextCursor;
}
