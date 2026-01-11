package com.study.api_gateway.api.activity.client;

import com.study.api_gateway.api.activity.dto.request.FeedTotalsRequest;
import com.study.api_gateway.api.activity.dto.response.FeedPageResponse;
import com.study.api_gateway.api.activity.dto.response.FeedTotalsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class ActivityClient {
	private final WebClient webClient;
	
	public ActivityClient(@Qualifier(value = "activitiesClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * POST /api/board/feed - 카테고리별 활동 총합 조회
	 */
	public Mono<FeedTotalsResponse> getFeedTotals(FeedTotalsRequest request) {
		String uriString = UriComponentsBuilder.fromPath("/api/board/feed")
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(FeedTotalsResponse.class);
	}
	
	/**
	 * GET /api/board/feed/{category} - 카테고리별 articleId 목록 조회 (페이징)
	 */
	public Mono<FeedPageResponse> getFeedByCategory(
			String category,
			String viewerId,
			String targetUserId,
			String cursor,
			Integer size,
			String sort
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/board/feed/{category}")
				.queryParam("targetUserId", targetUserId);
		
		if (viewerId != null) {
			builder.queryParam("viewerId", viewerId);
		}
		if (cursor != null) {
			builder.queryParam("cursor", cursor);
		}
		if (size != null) {
			builder.queryParam("size", size);
		}
		if (sort != null) {
			builder.queryParam("sort", sort);
		}
		
		String uriString = builder.buildAndExpand(category).toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(FeedPageResponse.class);
	}
}
