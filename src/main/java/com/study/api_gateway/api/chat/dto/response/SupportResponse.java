package com.study.api_gateway.api.chat.dto.response;

import com.study.api_gateway.api.chat.dto.enums.SupportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 관련 응답
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportResponse {
	private String roomId;
	private Long agentId;
	private SupportStatus status;
	private LocalDateTime createdAt;
}
