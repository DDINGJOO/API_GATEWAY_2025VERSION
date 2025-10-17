package com.study.api_gateway.dto.support.inquiry.request;

import com.study.api_gateway.dto.support.inquiry.InquiryCategory;
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
	private String writerId;
}
