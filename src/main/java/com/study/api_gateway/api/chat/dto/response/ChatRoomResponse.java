package com.study.api_gateway.api.chat.dto.response;

import com.study.api_gateway.api.chat.dto.enums.ChatRoomType;
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
	private String profileImage;
	private List<ParticipantInfo> participants;
	private String lastMessage;
	private LocalDateTime lastMessageAt;
	private Long unreadCount;
	private ContextInfo context;
	
	/**
	 * 채팅방 컨텍스트 정보 (PLACE_INQUIRY 타입일 때 사용)
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ContextInfo {
		private String contextType;
		private Long contextId;
		private String contextName;
	}
}
