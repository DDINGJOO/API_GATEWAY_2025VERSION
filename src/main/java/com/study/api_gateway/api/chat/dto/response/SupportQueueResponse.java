package com.study.api_gateway.api.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 상담 대기열 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportQueueResponse {
	private List<SupportQueueItem> queue;
	private Integer totalCount;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SupportQueueItem {
		private String roomId;
		private Long userId;
		private String category;
		private String lastMessage;
		private java.time.LocalDateTime createdAt;
	}
}
