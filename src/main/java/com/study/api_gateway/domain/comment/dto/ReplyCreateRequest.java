package com.study.api_gateway.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ReplyCreateRequest", description = "대댓글 생성 요청 바디",
		example = "{\n  \"writerId\": \"user-2\",\n  \"contents\": \"부모 댓글에 대한 대댓글입니다.\"\n}")
public class ReplyCreateRequest {
	@Schema(description = "작성자 ID", example = "user-2")
	private String writerId;
	@Schema(description = "대댓글 내용", example = "부모 댓글에 대한 대댓글입니다.")
	private String contents;
}
