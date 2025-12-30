package com.study.api_gateway.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Chat Server 호스트 문의 원본 응답 (프로필 병합 전)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalHostInquiryResponse {
	private String roomId;
	private Long guestId;
	private Long placeId;
	private String placeName;
	private String lastMessage;
	private LocalDateTime lastMessageAt;
	private Long unreadCount;
}
