package com.study.api_gateway.dto.gaechu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeCountResponse {
	private String referenceId;
	private Integer likeCount;
}
