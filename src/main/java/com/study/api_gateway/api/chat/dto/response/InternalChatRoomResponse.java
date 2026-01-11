package com.study.api_gateway.api.chat.dto.response;

import com.study.api_gateway.api.chat.dto.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat Server 원본 응답 (프로필 병합 전)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalChatRoomResponse {
	private String roomId;
	private ChatRoomType type;
	private String name;
	private List<Long> participantIds;
	private String lastMessage;
	private LocalDateTime lastMessageAt;
	private Long unreadCount;
}
