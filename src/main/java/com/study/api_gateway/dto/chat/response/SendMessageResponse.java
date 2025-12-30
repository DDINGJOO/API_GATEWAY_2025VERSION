package com.study.api_gateway.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 전송 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageResponse {
	private String messageId;
	private String roomId;
	private Long senderId;
	private String content;
	private LocalDateTime createdAt;
}
