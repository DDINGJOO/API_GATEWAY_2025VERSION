package com.study.api_gateway.dto.chat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "1:1 DM 채팅방 생성 요청")
public class CreateDmRoomRequest {
	
	@NotNull(message = "상대방 ID는 필수입니다")
	@Schema(description = "상대방 사용자 ID", example = "456", required = true)
	private Long recipientId;
	
	@Size(max = 5000, message = "초기 메시지는 최대 5000자까지 입력 가능합니다")
	@Schema(description = "초기 메시지 (선택)", example = "안녕하세요", maxLength = 5000)
	private String initialMessage;
}
