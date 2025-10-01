package com.study.api_gateway.dto.Article.request;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleCreateRequest {
	private String title;
	private String content;
	private String writerId;
	
	private List<String> imageUrls;
	
	
	private List<?> keywords;
	private Object board;
	
}
