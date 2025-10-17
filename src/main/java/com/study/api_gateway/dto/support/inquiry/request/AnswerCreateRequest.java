package com.study.api_gateway.dto.support.inquiry.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerCreateRequest {
	private String inquiryId;
	private String contents;
	private String writerId;
}
