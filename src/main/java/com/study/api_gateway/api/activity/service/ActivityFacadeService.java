package com.study.api_gateway.api.activity.service;

import com.study.api_gateway.api.activity.client.ActivityClient;
import com.study.api_gateway.api.activity.dto.request.FeedTotalsRequest;
import com.study.api_gateway.api.activity.dto.response.FeedPageResponse;
import com.study.api_gateway.api.activity.dto.response.FeedTotalsResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Activity 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class ActivityFacadeService {

	private final ActivityClient activityClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "activity-service";

	public Mono<FeedTotalsResponse> getFeedTotals(FeedTotalsRequest request) {
		return activityClient.getFeedTotals(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<FeedPageResponse> getFeedByCategory(
			String category,
			String viewerId,
			String targetUserId,
			String cursor,
			Integer size,
			String sort
	) {
		return activityClient.getFeedByCategory(category, viewerId, targetUserId, cursor, size, sort)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
