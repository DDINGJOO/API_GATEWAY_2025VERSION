package com.study.api_gateway.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 참여자 프로필 정보 (프로필 병합 후)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantInfo {
	private Long userId;
	private String nickname;
	private String profileImage;
}
