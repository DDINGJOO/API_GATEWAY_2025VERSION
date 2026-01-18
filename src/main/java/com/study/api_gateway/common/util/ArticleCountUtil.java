package com.study.api_gateway.common.util;

import com.study.api_gateway.api.article.dto.response.EnrichedArticleResponse;
import com.study.api_gateway.api.comment.client.CommentClient;
import com.study.api_gateway.api.gaechu.client.LikeClient;
import com.study.api_gateway.api.gaechu.dto.LikeCountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 게시글 카운트 보강 유틸리티
 * - 게시글 리스트에 댓글 수 및 좋아요 수를 배치 조회하여 주입합니다.
 * - N+1 문제 방지를 위해 배치 API를 사용합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleCountUtil {
	
	private final LikeClient likeClient;
	private final CommentClient commentClient;
	
	/**
	 * 게시글 리스트에 댓글 수 및 좋아요 수 추가
	 * - EnrichedArticleResponse 리스트에 commentCount와 likeCount를 배치 조회 후 주입합니다.
	 * - LikeClient와 CommentClient를 통해 배치 조회하여 N+1 문제를 방지합니다.
	 *
	 * @param articles   EnrichedArticleResponse 리스트
	 * @param categoryId 좋아요 카테고리 ID (예: "ARTICLE")
	 * @return 댓글 수와 좋아요 수가 주입된 EnrichedArticleResponse 리스트를 포함하는 Mono
	 */
	public Mono<List<EnrichedArticleResponse>> enrichWithCounts(
			List<EnrichedArticleResponse> articles,
			String categoryId) {
		
		if (articles == null || articles.isEmpty()) {
			return Mono.just(articles == null ? List.of() : articles);
		}
		
		// Extract article IDs
		List<String> articleIds = articles.stream()
				.map(EnrichedArticleResponse::getArticleId)
				.filter(Objects::nonNull)
				.toList();
		
		if (articleIds.isEmpty()) {
			return Mono.just(articles);
		}
		
		// Fetch like counts and comment counts in parallel (with fallback on error)
		Mono<List<LikeCountResponse>> likeCountsMono = likeClient.getLikeCounts(categoryId, articleIds)
				.onErrorReturn(List.of());
		Mono<Map<String, Integer>> commentCountsMono = commentClient.getCountsForArticles(articleIds)
				.onErrorReturn(Map.of());
		
		return Mono.zip(likeCountsMono, commentCountsMono)
				.map(tuple2 -> {
					// Build quick lookup maps for counts
					Map<String, Integer> likeCountMap = tuple2.getT1().stream()
							.filter(Objects::nonNull)
							.collect(Collectors.toMap(
									LikeCountResponse::getReferenceId,
									lc -> lc.getLikeCount() == null ? 0 : lc.getLikeCount(),
									(a, b) -> a // merge function for duplicate keys
							));
					Map<String, Integer> commentCountMap = tuple2.getT2();
					
					// Apply counts to each article
					articles.forEach(article -> {
						String articleId = article.getArticleId();
						if (articleId != null) {
							article.withCounts(
									commentCountMap.getOrDefault(articleId, 0),
									likeCountMap.getOrDefault(articleId, 0)
							);
						}
					});
					
					return articles;
				});
	}
}
