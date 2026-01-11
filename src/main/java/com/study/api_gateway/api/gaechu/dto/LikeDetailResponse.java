package com.study.api_gateway.api.gaechu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeDetailResponse {
	private String referenceId;
	private Integer likeCount;
	private List<String> likerIds;
}
