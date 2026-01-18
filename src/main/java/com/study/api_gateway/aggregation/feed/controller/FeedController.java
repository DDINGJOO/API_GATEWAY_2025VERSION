package com.study.api_gateway.aggregation.feed.controller;

import com.study.api_gateway.api.activity.client.ActivityClient;
import com.study.api_gateway.api.activity.dto.request.FeedTotalsRequest;
import com.study.api_gateway.api.activity.dto.response.EnrichedFeedPageResponse;
import com.study.api_gateway.api.activity.dto.response.FeedPageResponse;
import com.study.api_gateway.api.article.client.ArticleClient;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.common.util.ArticleCountUtil;
import com.study.api_gateway.common.util.UserIdValidator;
import com.study.api_gateway.enrichment.ProfileEnrichmentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/v1/activities/feed")
@RequiredArgsConstructor
@Validated
public class FeedController implements FeedApi {
	private final ActivityClient activityClient;
	private final ArticleClient articleClient;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final ArticleCountUtil articleCountUtil;
	private final ResponseFactory responseFactory;
	private final UserIdValidator userIdValidator;
	
	private final String categoryId = "ARTICLE";
	
	@Override
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> getFeedTotals(
			@RequestBody FeedTotalsRequest request,
			ServerHttpRequest req) {
		
		// JWT 토큰에서 viewerId 추출 (로그인하지 않은 경우 null)
		String viewerId = req.getHeaders().getFirst("X-User-Id");
		request.setViewerId(viewerId);
		
		// Validate required field
		if (request.getTargetUserId() == null || request.getTargetUserId().isBlank()) {
			return Mono.just(responseFactory.error("targetUserId is required", HttpStatus.BAD_REQUEST, req));
		}
		
		// Set default categories if not provided
		if (request.getCategories() == null || request.getCategories().isEmpty()) {
			request.setCategories(List.of("article", "comment", "like"));
		}
		
		return activityClient.getFeedTotals(request)
				.map(response -> responseFactory.ok(response, req))
				.onErrorResume(e ->
						Mono.just(responseFactory.error("Failed to fetch feed totals: " + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR, req))
				);
	}
	
	@Override
	@GetMapping("/me/totals")
	public Mono<ResponseEntity<BaseResponse>> getMyFeedTotals(ServerHttpRequest req) {
		
		// JWT 토큰에서 userId 추출
		String userId = req.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isBlank()) {
			return Mono.just(responseFactory.error("인증이 필요합니다.", HttpStatus.UNAUTHORIZED, req));
		}
		
		// 본인 피드 총합 조회 (targetUserId = viewerId = userId)
		FeedTotalsRequest request = FeedTotalsRequest.builder()
				.targetUserId(userId)
				.viewerId(userId)
				.categories(List.of("article", "comment", "like"))
				.build();
		
		return activityClient.getFeedTotals(request)
				.map(response -> responseFactory.ok(response, req))
				.onErrorResume(e ->
						Mono.just(responseFactory.error("Failed to fetch feed totals: " + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR, req))
				);
	}
	
	@Override
	@GetMapping("/{category}")
	public Mono<ResponseEntity<BaseResponse>> getFeedByCategory(
			@PathVariable String category,
			@RequestParam String targetUserId,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false, defaultValue = "20") Integer size,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			ServerHttpRequest req) {
		
		// JWT 토큰에서 viewerId 추출 (로그인하지 않은 경우 null)
		String viewerId = req.getHeaders().getFirst("X-User-Id");
		
		// Validate required parameter
		if (targetUserId == null || targetUserId.isBlank()) {
			return Mono.just(responseFactory.error("targetUserId is required", HttpStatus.BAD_REQUEST, req));
		}
		
		// Validate category
		if (!List.of("article", "comment", "like").contains(category)) {
			return Mono.just(responseFactory.error("Invalid category. Must be one of: article, comment, like",
					HttpStatus.BAD_REQUEST, req));
		}
		
		// Check permission for 'like' category (본인만 조회 가능)
		if ("like".equals(category)) {
			// 토큰의 userId가 targetUserId와 일치하는지 검증
			return userIdValidator.validateReactive(req, targetUserId)
					.then(activityClient.getFeedByCategory(category, viewerId, targetUserId, cursor, size, sort))
					.flatMap(feedResponse -> enrichFeedResponse(feedResponse))
					.map(enrichedResponse -> responseFactory.ok(enrichedResponse, req))
					.onErrorResume(e ->
							Mono.just(responseFactory.error("Failed to fetch feed: " + e.getMessage(),
									HttpStatus.INTERNAL_SERVER_ERROR, req))
					);
		}
		
		// article, comment 카테고리는 공개 (검증 불필요)
		return activityClient.getFeedByCategory(category, viewerId, targetUserId, cursor, size, sort)
				.flatMap(feedResponse -> enrichFeedResponse(feedResponse))
				.map(enrichedResponse -> responseFactory.ok(enrichedResponse, req))
				.onErrorResume(e ->
						Mono.just(responseFactory.error("Failed to fetch feed: " + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR, req))
				);
	}
	
	@Override
	@GetMapping("/me/{category}")
	public Mono<ResponseEntity<BaseResponse>> getMyFeedByCategory(
			@PathVariable String category,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false, defaultValue = "20") Integer size,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			ServerHttpRequest req) {
		
		// JWT 토큰에서 userId 추출
		String userId = req.getHeaders().getFirst("X-User-Id");
		if (userId == null || userId.isBlank()) {
			return Mono.just(responseFactory.error("인증이 필요합니다.", HttpStatus.UNAUTHORIZED, req));
		}
		
		// Validate category
		if (!List.of("article", "comment", "like").contains(category)) {
			return Mono.just(responseFactory.error("Invalid category. Must be one of: article, comment, like",
					HttpStatus.BAD_REQUEST, req));
		}
		
		// 본인 피드 조회 (targetUserId = viewerId = userId)
		return activityClient.getFeedByCategory(category, userId, userId, cursor, size, sort)
				.flatMap(feedResponse -> enrichFeedResponse(feedResponse))
				.map(enrichedResponse -> responseFactory.ok(enrichedResponse, req))
				.onErrorResume(e ->
						Mono.just(responseFactory.error("Failed to fetch feed: " + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR, req))
				);
	}
	
	/**
	 * Feed 응답을 Article 정보, 프로필 정보, 댓글/좋아요 수로 보강합니다.
	 * <p>
	 * 처리 플로우:
	 * 1. FeedPageResponse에서 articleIds 추출
	 * 2. ArticleClient를 통해 Article 도메인에서 게시글 상세 정보를 배치 조회
	 * 3. ProfileEnrichmentUtil을 통해 프로필 정보를 배치 조회 및 주입 (캐시 우선)
	 * 4. ArticleCountUtil을 통해 좋아요 수와 댓글 수 조회 및 주입
	 * 5. EnrichedFeedPageResponse로 변환하여 반환
	 * </p>
	 *
	 * @param feedResponse FeedPageResponse (articleIds, nextCursor)
	 * @return 프로필 정보 및 카운트가 주입된 EnrichedFeedPageResponse
	 */
	private Mono<EnrichedFeedPageResponse> enrichFeedResponse(
			FeedPageResponse feedResponse) {
		
		// articleIds가 없으면 빈 응답 반환
		if (feedResponse == null || feedResponse.getArticleIds() == null || feedResponse.getArticleIds().isEmpty()) {
			return Mono.just(EnrichedFeedPageResponse.builder()
					.articles(List.of())
					.nextCursor(feedResponse != null ? feedResponse.getNextCursor() : null)
					.build());
		}
		
		// 1. Article 도메인에서 게시글 상세 정보 배치 조회
		return articleClient.getBulkArticles(feedResponse.getArticleIds())
				.flatMap(articles -> {
					// articles가 null이거나 비어있으면 빈 응답 반환
					if (articles == null || articles.isEmpty()) {
						return Mono.just(EnrichedFeedPageResponse.builder()
								.articles(List.of())
								.nextCursor(feedResponse.getNextCursor())
								.build());
					}
					
					// 2. ProfileEnrichmentUtil을 통해 프로필 정보 배치 조회 및 주입
					return profileEnrichmentUtil.enrichArticleList(articles)
							.flatMap(enrichedArticles ->
									// 3. ArticleCountUtil을 통해 좋아요 수 및 댓글 수 조회 및 주입
									articleCountUtil.enrichWithCounts(enrichedArticles, categoryId)
							)
							.map(enrichedArticles -> EnrichedFeedPageResponse.builder()
									.articles(enrichedArticles)
									.nextCursor(feedResponse.getNextCursor())
									.build());
				});
	}
}
