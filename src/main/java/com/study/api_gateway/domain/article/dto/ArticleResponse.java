package com.study.api_gateway.domain.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ArticleResponse", description = "게시글 단건 응답",
		example = "{\n  \"articleId\": \"art_001\",\n  \"title\": \"첫 게시글\",\n  \"content\": \"안녕하세요 밴더입니다.\",\n  \"writerId\": \"user_123\",\n  \"board\": { \"1\": \"FREE\" },\n  \"LastestUpdateId\": \"2025-01-10T12:34:56\",\n  \"imageUrls\": { \"img_1\": \"https://cdn.example.com/images/img_1.png\" },\n  \"keywords\": { \"10\": \"MUSIC\" }\n}")
public class ArticleResponse {
	@Schema(description = "게시글 ID", example = "art_001")
	private String articleId;
	@Schema(description = "제목", example = "첫 게시글")
	private String title;
	@Schema(description = "내용", example = "안녕하세요 밴더입니다.")
	private String content;
	@Schema(description = "작성자 ID", example = "user_123")
	private String writerId;
	
	@Schema(description = "보드 정보(식별자-이름 매핑)", example = "{\n  \"1\": \"FREE\"\n}")
	private Map<Long, String> board;
	
	@Schema(description = "마지막 수정 시각", example = "2025-01-10T12:34:56")
	private LocalDateTime LastestUpdateId;
	@Schema(description = "이미지 ID-URL 매핑", example = "{\n  \"img_1\": \"https://cdn.example.com/images/img_1.png\"\n}")
	private Map<String, String> imageUrls;
	@Schema(description = "키워드 정보(식별자-이름 매핑)", example = "{\n  \"10\": \"MUSIC\"\n}")
	private Map<Long, String> keywords;
	
}
