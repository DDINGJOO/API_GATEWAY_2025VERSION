package com.study.api_gateway.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 응답 (프로필 병합 후)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
	private String messageId;
	private String roomId;
	private SenderInfo sender;
	private String content;
	private Integer readCount;
	private Boolean deleted;
	private LocalDateTime createdAt;
}
