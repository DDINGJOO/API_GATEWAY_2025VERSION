package com.study.api_gateway.domain.like;

import com.study.api_gateway.domain.like.dto.LikeCountResponse;
import com.study.api_gateway.domain.like.dto.LikeDetailResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class LikeClient {
	
	private final WebClient webClient;
	
	public LikeClient(@Qualifier(value = "likeWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * POST /api/likes/{categoryId}/{referenceId} - 좋아요/좋아요 취소
	 */
	public Mono<Void> likeOrUnlike(String categoryId, String referenceId, String likerId, boolean isLike) {
		String uri = UriComponentsBuilder.fromPath("/api/likes/{categoryId}/{referenceId}")
				.queryParam("likerId", likerId)
				.queryParam("isLike", isLike)
				.buildAndExpand(categoryId, referenceId)
				.toUriString();
		
		return webClient.post()
				.uri(uri)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	/**
	 * GET /api/likes/{categoryId}/{referenceId} - 좋아요 상세 조회
	 */
	public Mono<LikeDetailResponse> getLikeDetail(String categoryId, String referenceId) {
		String uri = UriComponentsBuilder.fromPath("/api/likes/{categoryId}/{referenceId}")
				.buildAndExpand(categoryId, referenceId)
				.toUriString();
		
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(LikeDetailResponse.class);
	}
	
	/**
	 * POST /api/likes/{categoryId}/counts - 여러 reference의 좋아요 수 조회
	 */
	public Mono<List<LikeCountResponse>> getLikeCounts(String categoryId, List<String> referenceIds) {
		String uri = UriComponentsBuilder.fromPath("/api/likes/{categoryId}/counts")
				.buildAndExpand(categoryId)
				.toUriString();
		
		return webClient.post()
				.uri(uri)
				.bodyValue(referenceIds)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	/**
	 * GET /api/likes/{categoryId}/users/{userId}/count - 특정 사용자가 받은 좋아요 수
	 */
	public Mono<Integer> getUserLikedCounts(String categoryId, String userId) {
		String uri = UriComponentsBuilder.fromPath("/api/likes/{categoryId}/users/{userId}/count")
				.buildAndExpand(categoryId, userId)
				.toUriString();
		
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(Integer.class);
	}
}
