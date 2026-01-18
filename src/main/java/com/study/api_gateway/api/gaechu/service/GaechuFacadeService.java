package com.study.api_gateway.api.gaechu.service;

import com.study.api_gateway.api.gaechu.client.LikeClient;
import com.study.api_gateway.api.gaechu.dto.LikeCountResponse;
import com.study.api_gateway.api.gaechu.dto.LikeDetailResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gaechu(좋아요) 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class GaechuFacadeService {
	
	private static final String SERVICE_NAME = "gaechu-service";
	private final LikeClient likeClient;
	private final ResilienceOperator resilience;
	
	public Mono<Void> likeOrUnlike(String categoryId, String referenceId, String likerId, boolean isLike) {
		return likeClient.likeOrUnlike(categoryId, referenceId, likerId, isLike)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<LikeDetailResponse> getLikeDetail(String categoryId, String referenceId) {
		return likeClient.getLikeDetail(categoryId, referenceId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<LikeCountResponse>> getLikeCounts(String categoryId, List<String> referenceIds) {
		return likeClient.getLikeCounts(categoryId, referenceIds)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<LikeCountResponse>> getUserLikedCounts(String categoryId, String userId) {
		return likeClient.getUserLikedCounts(categoryId, userId)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
