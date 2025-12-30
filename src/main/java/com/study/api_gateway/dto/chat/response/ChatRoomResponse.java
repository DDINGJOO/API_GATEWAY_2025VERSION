package com.study.api_gateway.dto.chat.response;

import com.study.api_gateway.dto.chat.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 응답 (프로필 병합 후)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
	private String roomId;
	private ChatRoomType type;
	private String name;
	private List<ParticipantInfo> participants;
	private String lastMessage;
	private LocalDateTime lastMessageAt;
	private Long unreadCount;
}
