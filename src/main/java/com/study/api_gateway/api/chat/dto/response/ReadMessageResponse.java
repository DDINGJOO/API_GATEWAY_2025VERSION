package com.study.api_gateway.api.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 읽음 처리 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadMessageResponse {
	private String roomId;
	private LocalDateTime lastReadAt;
	private Long unreadCount;
}
