package com.study.api_gateway.dto.comment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CombinedCommentCreateRequest", description = "루트/대댓글 통합 생성 요청 바디",
		example = "{\n  \"articleId\": \"article-1\",\n  \"writerId\": \"user-1\",\n  \"contents\": \"첫 댓글 또는 대댓글 내용\"\n}")
public class CombinedCommentCreateRequest {
	@Schema(description = "대상 게시글 ID (루트 댓글 생성 시 필수)", example = "article-1")
	private String articleId;
	@Schema(description = "작성자 ID", example = "user-1")
	private String writerId;
	@Schema(description = "댓글 내용", example = "내용")
	private String contents;
}
