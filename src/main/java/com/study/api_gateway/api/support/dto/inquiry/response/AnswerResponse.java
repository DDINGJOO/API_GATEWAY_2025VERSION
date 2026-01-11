package com.study.api_gateway.api.support.dto.inquiry.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
	private String id;
	private String inquiryId;
	private String writerId;
	private String contents;
	private LocalDateTime createdAt;
}
