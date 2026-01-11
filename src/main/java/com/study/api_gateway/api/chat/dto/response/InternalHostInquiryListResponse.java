package com.study.api_gateway.api.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Chat Server 호스트 문의 목록 원본 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalHostInquiryListResponse {
	private List<InternalHostInquiryResponse> inquiries;
}
