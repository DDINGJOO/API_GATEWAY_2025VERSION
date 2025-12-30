package com.study.api_gateway.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 호스트 문의 응답 (프로필 병합 후)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostInquiryResponse {
	private String roomId;
	private ParticipantInfo guest;
	private Long placeId;
	private String placeName;
	private String lastMessage;
	private LocalDateTime lastMessageAt;
	private Long unreadCount;
}
