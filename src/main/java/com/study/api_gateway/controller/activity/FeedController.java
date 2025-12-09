package com.study.api_gateway.controller.activity;

import com.study.api_gateway.client.ActivityClient;
import com.study.api_gateway.client.ArticleClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.activity.request.FeedTotalsRequest;
import com.study.api_gateway.dto.activity.response.EnrichedFeedPageResponse;
import com.study.api_gateway.util.ArticleCountUtil;
import com.study.api_gateway.util.ProfileEnrichmentUtil;
import com.study.api_gateway.util.ResponseFactory;
import com.study.api_gateway.util.UserIdValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class FeedController {
	private final ActivityClient activityClient;
	private final ArticleClient articleClient;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final ArticleCountUtil articleCountUtil;
	private final ResponseFactory responseFactory;
	private final UserIdValidator userIdValidator;
	
	private final String categoryId = "ARTICLE";
	
	@Operation(summary = "피드 활동 총합 조회",
			description = "특정 사용자의 카테고리별 활동 총합(article, comment, like)과 조회자와 대상의 동일 여부를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "FeedTotalsSuccess",
									value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"totals\": {\n      \"article\": 12,\n      \"comment\": 34,\n      \"like\": 5\n    },\n    \"isOwner\": false\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/activities/feed\"\n  }\n}"))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (targetUserId 누락)",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "BadRequest",
									value = "{\n  \"isSuccess\": false,\n  \"code\": 400,\n  \"data\": \"targetUserId is required\",\n  \"request\": {\n    \"path\": \"/bff/v1/activities/feed\"\n  }\n}")))
	})
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> getFeedTotals(
			@RequestBody FeedTotalsRequest request,
			ServerHttpRequest req) {
		
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
	
	@Operation(summary = "카테고리별 피드 조회",
			description = "지정된 카테고리(article, comment, like)에 대해 articleId 목록을 페이징으로 조회합니다. like 카테고리는 본인만 조회 가능합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "FeedPageSuccess",
									value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleIds\": [\"a-100\", \"a-99\", \"a-98\"],\n    \"nextCursor\": \"a-98\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/activities/feed/article\"\n  }\n}"))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 파라미터 누락)",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "BadRequest",
									value = "{\n  \"isSuccess\": false,\n  \"code\": 400,\n  \"data\": \"targetUserId is required\",\n  \"request\": {\n    \"path\": \"/bff/v1/activities/feed/article\"\n  }\n}"))),
			@ApiResponse(responseCode = "403", description = "권한 없음 (like 카테고리는 본인만 조회 가능)",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "Forbidden",
									value = "{\n  \"isSuccess\": false,\n  \"code\": 403,\n  \"data\": \"Access denied: like category can only be accessed by the owner\",\n  \"request\": {\n    \"path\": \"/bff/v1/activities/feed/like\"\n  }\n}")))
	})
	@GetMapping("/{category}")
	public Mono<ResponseEntity<BaseResponse>> getFeedByCategory(
			@PathVariable String category,
			@RequestParam String targetUserId,
			@RequestParam(required = false) String viewerId,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false, defaultValue = "20") Integer size,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			ServerHttpRequest req) {
		
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
			com.study.api_gateway.dto.activity.response.FeedPageResponse feedResponse) {
		
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
