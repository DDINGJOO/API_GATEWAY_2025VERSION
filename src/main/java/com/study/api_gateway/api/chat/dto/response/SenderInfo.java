package com.study.api_gateway.api.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 발신자 프로필 정보 (프로필 병합 후)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SenderInfo {
	private Long userId;
	private String nickname;
	private String profileImage;
}
