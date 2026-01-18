package com.study.api_gateway.api.chat.dto.response;

import com.study.api_gateway.api.chat.dto.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공간 문의 생성 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceInquiryResponse {
	private String roomId;
	private ChatRoomType type;
	private ContextInfo context;
	private LocalDateTime createdAt;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ContextInfo {
		private String contextType;
		private Long contextId;
		private String contextName;
	}
}
