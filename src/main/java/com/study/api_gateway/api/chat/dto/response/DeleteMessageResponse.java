package com.study.api_gateway.api.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 삭제 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteMessageResponse {
	private String messageId;
	private Boolean hardDeleted;
	private LocalDateTime deletedAt;
}
