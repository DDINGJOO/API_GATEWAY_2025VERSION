package com.study.api_gateway.dto.activity.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedTotalsResponse {
	private Map<String, Long> totals;
	private Boolean isOwner;
}
