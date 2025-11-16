package com.study.api_gateway.dto.support.inquiry.request;

import com.study.api_gateway.dto.support.inquiry.InquiryCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCreateRequest {
	private String title;
	private String contents;
	private InquiryCategory category;
	@Schema(description = "작성자 ID (서버에서 토큰으로부터 자동 설정, 클라이언트는 전송 불필요)", accessMode = Schema.AccessMode.READ_ONLY)
	private String writerId;
}
