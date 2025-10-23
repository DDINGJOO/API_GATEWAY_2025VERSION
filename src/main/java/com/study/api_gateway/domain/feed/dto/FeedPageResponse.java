package com.study.api_gateway.domain.feed.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedPageResponse {
	private List<String> articleIds;
	private String nextCursor;
}
