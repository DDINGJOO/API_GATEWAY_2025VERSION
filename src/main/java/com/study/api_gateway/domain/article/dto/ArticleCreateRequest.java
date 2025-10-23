package com.study.api_gateway.domain.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ArticleCreateRequest", description = "게시글 생성/수정 요청 바디",
		example = "{\n  \"title\": \"공연 함께 하실 분\",\n  \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n  \"writerId\": \"user_123\",\n  \"imageUrls\": [\"img_1\", \"img_2\"],\n  \"keywords\": [10, 12],\n  \"board\": 1\n}")
public class ArticleCreateRequest {
	@Schema(description = "제목", example = "공연 함께 하실 분")
	private String title;
	@Schema(description = "내용", example = "같이 즐겁게 공연하실 분을 찾습니다.")
	private String content;
	@Schema(description = "작성자 ID", example = "user_123")
	private String writerId;
	
	@Schema(description = "이미지 ID 목록", example = "[\"img_1\", \"img_2\"]")
	private List<String> imageUrls;
	
	@Schema(description = "키워드 ID 목록", example = "[10, 12]")
	private List<?> keywords;
	@Schema(description = "보드 식별자 또는 객체", example = "1")
	private Object board;
	
}
