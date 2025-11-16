package com.study.api_gateway.dto.comment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CommentUpdateRequest", description = "댓글 내용 수정 요청 바디",
		example = "{\n  \"contents\": \"수정한 내용\"\n}")
public class CommentUpdateRequest {
	@Schema(description = "작성자 ID (서버에서 토큰으로부터 자동 설정, 클라이언트는 전송 불필요)", example = "user-1", accessMode = Schema.AccessMode.READ_ONLY)
    private String writerId;
	@Schema(description = "수정할 댓글 내용", example = "수정한 내용")
    private String contents;
}
