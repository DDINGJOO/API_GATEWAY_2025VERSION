package com.study.api_gateway.api.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 호스트 문의 목록 응답 (프로필 병합 후)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostInquiryListResponse {
	private List<HostInquiryResponse> inquiries;
}
