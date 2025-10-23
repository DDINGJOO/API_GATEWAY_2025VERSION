package com.study.api_gateway.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUserSummaryResponse {
	private String userId;
	private String nickname;
	private String profileImageUrl;
}
