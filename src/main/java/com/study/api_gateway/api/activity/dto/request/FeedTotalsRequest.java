package com.study.api_gateway.api.activity.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedTotalsRequest {
	private String targetUserId;
	private String viewerId;
	private List<String> categories;
}
