package com.study.api_gateway.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Chat Server 메시지 목록 원본 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalMessageListResponse {
	private List<InternalMessageResponse> messages;
	private String nextCursor;
	private Boolean hasMore;
}
