package com.study.api_gateway.dto.Article.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleResponse {
	private String articleId;
	private String title;
	private String content;
	private String writerId;
	
	private Map<Long, String> board;
	
	private LocalDateTime LastestUpdateId;
	private Map<String, String> imageUrls;
	private Map<Long, String> keywords;
	
}
