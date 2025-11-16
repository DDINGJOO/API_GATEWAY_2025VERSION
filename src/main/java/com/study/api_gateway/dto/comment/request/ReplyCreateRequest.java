package com.study.api_gateway.dto.comment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ReplyCreateRequest", description = "대댓글 생성 요청 바디",
		example = "{\n  \"contents\": \"부모 댓글에 대한 대댓글입니다.\"\n}")
public class ReplyCreateRequest {
	@Schema(description = "작성자 ID (서버에서 토큰으로부터 자동 설정, 클라이언트는 전송 불필요)", example = "user-2", accessMode = Schema.AccessMode.READ_ONLY)
    private String writerId;
	@Schema(description = "대댓글 내용", example = "부모 댓글에 대한 대댓글입니다.")
    private String contents;
}
