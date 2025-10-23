package com.study.api_gateway.domain.support.dto.inquiry;

import com.study.api_gateway.domain.support.enums.InquiryCategory;
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
