package com.study.api_gateway.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Chat Server 메시지 원본 응답 (프로필 병합 전)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalMessageResponse {
	private String messageId;
	private String roomId;
	private Long senderId;
	private String content;
	private Integer readCount;
	private Boolean deleted;
	private LocalDateTime createdAt;
}
