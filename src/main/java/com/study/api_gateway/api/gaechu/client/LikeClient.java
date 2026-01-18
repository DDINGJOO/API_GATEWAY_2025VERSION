package com.study.api_gateway.api.gaechu.client;

import com.study.api_gateway.api.gaechu.dto.LikeCountResponse;
import com.study.api_gateway.api.gaechu.dto.LikeDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class LikeClient {
	
	private final WebClient webClient;
	
	public LikeClient(@Qualifier(value = "gaechuWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	// 1) 좋아요/좋아요 취소
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
	
	// 2) 특정 reference + category 조합의 상세 조회
	public Mono<LikeDetailResponse> getLikeDetail(String categoryId, String referenceId) {
		String uri = UriComponentsBuilder.fromPath("/api/likes/detail/{categoryId}/{referenceId}")
				.buildAndExpand(categoryId, referenceId)
				.toUriString();
		
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(LikeDetailResponse.class);
	}
	
	// 3) 여러 referenceId에 대한 like count 일괄 조회
	public Mono<List<LikeCountResponse>> getLikeCounts(String categoryId, List<String> referenceIds) {
		String base = UriComponentsBuilder.fromPath("/api/likes/count/{categoryId}")
				.buildAndExpand(categoryId)
				.toUriString();
		
		UriComponentsBuilder q = UriComponentsBuilder.fromUriString(base);
		if (referenceIds != null) {
			for (String id : referenceIds) {
				q.queryParam("referenceIds", id);
			}
		}
		String uri = q.toUriString();
		
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<LikeCountResponse>>() {
				});
	}
	
	// 4) 특정 사용자(작성자)가 좋아요한 레퍼런스 목록과 각 like count 조회
	public Mono<List<LikeCountResponse>> getUserLikedCounts(String categoryId, String userId) {
		String uri = UriComponentsBuilder.fromPath("/api/likes/count/{categoryId}/{userId}")
				.buildAndExpand(categoryId, userId)
				.toUriString();
		
		log.info("=== LikeClient.getUserLikedCounts === requesting: {}", uri);
		
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<LikeCountResponse>>() {
				})
				.doOnError(e -> log.error("=== LikeClient.getUserLikedCounts ERROR === {}", e.getMessage()));
	}
}
