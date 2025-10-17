package com.study.api_gateway.dto.support.inquiry.response;

import com.study.api_gateway.dto.support.inquiry.InquiryCategory;
import com.study.api_gateway.dto.support.inquiry.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {
	private String id;
	private String title;
	private String contents;
	private InquiryCategory category;
	private InquiryStatus status;
	private String writerId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime answeredAt;
	private Boolean hasAnswer;
	private AnswerResponse answer;
}
