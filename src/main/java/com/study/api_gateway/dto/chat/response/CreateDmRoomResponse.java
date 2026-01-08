package com.study.api_gateway.dto.chat.response;

import com.study.api_gateway.dto.chat.enums.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "1:1 DM 채팅방 생성 응답")
public class CreateDmRoomResponse {
	
	@Schema(description = "채팅방 ID", example = "room-uuid")
	private String roomId;
	
	@Schema(description = "채팅방 타입", example = "DM")
	private ChatRoomType type;
	
	@Schema(description = "채팅방 이름 (상대방 닉네임)", example = "홍길동")
	private String name;
	
	@Schema(description = "채팅방 이미지 (상대방 프로필 이미지)", example = "https://example.com/profile.jpg")
	private String profileImage;
	
	@Schema(description = "참여자 ID 목록", example = "[123, 456]")
	private List<Long> participantIds;
	
	@Schema(description = "생성 일시")
	private LocalDateTime createdAt;
	
	@Schema(description = "새로 생성된 채팅방 여부 (false면 기존 채팅방 반환)", example = "true")
	private Boolean isNewRoom;
}
