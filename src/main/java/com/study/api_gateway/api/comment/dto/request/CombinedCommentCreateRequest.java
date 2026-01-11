package com.study.api_gateway.api.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CombinedCommentCreateRequest", description = "루트/대댓글 통합 생성 요청 바디",
		example = "{\n  \"articleId\": \"article-1\",\n  \"contents\": \"첫 댓글 또는 대댓글 내용\"\n}")
public class CombinedCommentCreateRequest {
	@Schema(description = "대상 게시글 ID (루트 댓글 생성 시 필수)", example = "article-1")
	private String articleId;
	@Schema(description = "작성자 ID (서버에서 토큰으로부터 자동 설정, 클라이언트는 전송 불필요)", example = "user-1", accessMode = Schema.AccessMode.READ_ONLY)
	private String writerId;
	@Schema(description = "댓글 내용", example = "내용")
	private String contents;
}
