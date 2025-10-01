package com.study.api_gateway.dto.Article.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleCursorPageResponse {
	private List<ArticleResponse> items;
	// 다음 페이지 요청 시 사용할 수정일시 (updated_at)
	private LocalDateTime nextCursorUpdatedAt;
	private String nextCursorId;
	private boolean hasNext;
	private int size;
}
