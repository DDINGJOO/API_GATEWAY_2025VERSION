package com.study.api_gateway.controller.article;

import com.study.api_gateway.client.ArticleClient;
import com.study.api_gateway.client.CommentClient;
import com.study.api_gateway.client.LikeClient;
import com.study.api_gateway.dto.Article.request.ArticleCreateRequest;
import com.study.api_gateway.dto.Article.response.ArticleResponse;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.gaechu.LikeCountResponse;
import com.study.api_gateway.dto.gaechu.LikeDetailResponse;
import com.study.api_gateway.service.ImageConfirmService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bff/v1/communities/articles/regular")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ArticleController {
	private final ArticleClient articleClient;
	private final CommentClient commentClient;
	private final LikeClient likeClient;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final UserIdValidator userIdValidator;
	
	private final String categoryId = "ARTICLE";
	
	@Operation(summary = "일반 게시글 생성")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ArticleCreateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"article-1\",\n    \"title\": \"제목\",\n    \"content\": \"내용\",\n    \"imageUrls\": [\"img-1\"]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/regular\"\n  }\n}")))
	})
	@PostMapping()
	public Mono<ResponseEntity<BaseResponse>> postArticle(@RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		String userId = userIdValidator.extractTokenUserId(req);
		request.setWriterId(userId);
		
		// 1. 초기 요청 정보 로깅
		log.info("[게시글 생성 시작] userId: {}, title: {}, imageIds: {}, imageCount: {}",
				userId, request.getTitle(), request.getImageIds(),
				request.getImageIds() != null ? request.getImageIds().size() : 0);
		
		return articleClient.postArticle(request)
				.doOnNext(result -> {
					// 2. Article 서버 응답 성공 로깅
					log.info("[Article 서버 응답 성공] articleId: {}, userId: {}, title: {}",
							result.getArticleId(), userId, request.getTitle());
				})
				.doOnError(error -> {
					// 3. Article 서버 에러 로깅
					log.error("[Article 서버 에러] userId: {}, title: {}, error: {}",
							userId, request.getTitle(), error.getMessage(), error);
				})
				.flatMap(result -> {
					List<String> imageIds = request.getImageIds();
					
					if (imageIds != null && !imageIds.isEmpty()) {
						log.info("[이미지 처리 시작] articleId: {}, imageIds: {}, 50ms 지연 후 확정 요청",
								result.getArticleId(), imageIds);
						
						// 짧은 지연 후 이미지 확정 (게시글 저장 시간 확보)
						return Mono.delay(Duration.ofMillis(50))
								.doOnNext(tick -> {
									log.info("[이미지 확정 요청 전송] articleId: {}, imageIds: {} - Image 서버로 요청 전송 중",
											result.getArticleId(), imageIds);
								})
								.then(imageConfirmService.confirmImage(result.getArticleId(), imageIds))
								.doOnSuccess(v -> {
									log.info("[이미지 확정 성공] articleId: {}, imageIds: {} - Image 서버 처리 완료",
											result.getArticleId(), imageIds);
								})
								.doOnError(error -> {
									log.error("[이미지 확정 실패] articleId: {}, imageIds: {}, error: {} - Image 서버 오류",
											result.getArticleId(), imageIds, error.getMessage(), error);
								})
								.thenReturn(responseFactory.ok(result, req))
								.onErrorReturn(responseFactory.ok(result, req)) // 이미지 실패해도 게시글은 성공
								.doOnNext(response -> {
									log.info("[게시글 생성 완료 WITH 이미지] articleId: {}, status: {}, imageCount: {}",
											result.getArticleId(), response.getStatusCode(), imageIds.size());
								});
					} else {
						log.info("[이미지 없음] articleId: {} - 이미지 확정 과정 생략", result.getArticleId());
					}
					
					return Mono.just(responseFactory.ok(result, req))
							.doOnNext(response -> {
								log.info("[게시글 생성 완료 NO 이미지] articleId: {}, status: {}",
										result.getArticleId(), response.getStatusCode());
							});
				})
				.onErrorResume(error -> {
					return Mono.just(responseFactory.error("게시글 생성 실패", HttpStatus.INTERNAL_SERVER_ERROR, req));
				});
	}
	
	
	@Operation(summary = "일반 게시글 수정")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ArticleUpdateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"article-1\",\n    \"title\": \"수정된 제목\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/regular/{articleId}\"\n  }\n}")))
	})
	@PutMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> updateArticle(@PathVariable String articleId, @RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		// 1. 토큰에서 userId 추출
		String userId = userIdValidator.extractTokenUserId(req);
		request.setWriterId(userId);
		
		// 2. Article 조회하여 실제 작성자 확인
		return articleClient.getArticle(articleId)
				.flatMap(article -> userIdValidator.validateOwnership(req, article.getWriterId(), "게시글"))
				// 3. 검증 통과 후 수정 진행
				.then(articleClient.updateArticle(articleId, request))
				.flatMap(result -> {
					List<String> imageIds = request.getImageIds();
					if (imageIds != null && !imageIds.isEmpty()) {
						return imageConfirmService.confirmImage(articleId, imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Operation(summary = "일반 게시글 삭제")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ArticleDeleteSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"deleted\",\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/regular/{articleId}\"\n  }\n}")))
	})
	@DeleteMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> deleteArticle(@PathVariable String articleId, ServerHttpRequest req) {
		// 1. Article 조회하여 실제 작성자 확인
		return articleClient.getArticle(articleId)
				.flatMap(article -> userIdValidator.validateOwnership(req, article.getWriterId(), "게시글"))
				// 2. 검증 통과 후 삭제 진행
				.then(articleClient.deleteArticle(articleId))
				.thenReturn(responseFactory.ok("deleted", req, HttpStatus.OK));
	}
	
	@Operation(summary = "일반 게시글 단건 조회(댓글 포함)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ArticleDetailWithComments", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"article\": {\n      \"articleId\": \"42840044-0f3e-482c-b5d5-0883af43e63e\",\n      \"title\": \"공연 함께 하실 분\",\n      \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n      \"writerId\": \"user_123\",\n      \"board\": { \"1\": \"공지사항\" },\n      \"imageUrls\": {},\n      \"keywords\": { \"10\": \"중요\" },\n      \"lastestUpdateId\": \"2025-10-11T17:52:27\"\n    },\n    \"comments\": [\n      { \"commentId\": \"c1\", \"writerId\": \"user_123\", \"contents\": \"첫 댓글\", \"isOwn\": true, \"replies\": [] }\n    ],\n    \"likeDetail\": { \"likeCount\": 0, \"isOwn\": false }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/regular/{articleId}\"\n  }\n}")))
	})
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getArticle(@PathVariable String articleId, ServerHttpRequest req) {
		return Mono.zip(
						articleClient.getArticle(articleId),
						commentClient.getCommentsByArticle(articleId, 0, 10, "visibleCount")
								.onErrorReturn(List.of()),
						likeClient.getLikeDetail(categoryId, articleId)
								.onErrorReturn(null)
				)
				.flatMap(tuple3 -> {
					// Try to resolve current userId from headers (placeholder until token parsing is added)
					String currentUserId = resolveCurrentUserId(req);
					
					// Build likeDetail without referenceId and likerIds; add isOwn if current user liked
					LikeDetailResponse ld = tuple3.getT3();
					Map<String, Object> likeDetail = new LinkedHashMap<>();
					int likeCount = ld == null || ld.getLikeCount() == null ? 0 : ld.getLikeCount();
					likeDetail.put("likeCount", likeCount);
					boolean isOwnLike = false;
					if (currentUserId != null && ld != null && ld.getLikerIds() != null) {
						isOwnLike = ld.getLikerIds().stream().filter(Objects::nonNull).anyMatch(currentUserId::equals);
					}
					likeDetail.put("isOwn", isOwnLike);
					
					// Sanitize comments: remove keys referenceId and articleId; add isOwn if writerId == currentUserId; handle nested replies
					List<Map<String, Object>> rawComments = tuple3.getT2();
					List<Map<String, Object>> comments = rawComments == null ? List.of() : rawComments.stream()
							.map(c -> sanitizeCommentMap(c, currentUserId))
							.toList();
					
					// Build article map to allow adding profile info
					ArticleResponse ar = tuple3.getT1();
					Map<String, Object> articleMap = new LinkedHashMap<>();
					if (ar != null) {
						articleMap.put("articleId", ar.getArticleId());
						articleMap.put("title", ar.getTitle());
						articleMap.put("content", ar.getContent());
						articleMap.put("writerId", ar.getWriterId());
						articleMap.put("board", ar.getBoard());
						articleMap.put("status", ar.getStatus());
						articleMap.put("viewCount", ar.getViewCount());
						articleMap.put("firstImageUrl", ar.getFirstImageUrl());
						articleMap.put("createdAt", ar.getCreatedAt());
						articleMap.put("updatedAt", ar.getUpdatedAt());
						articleMap.put("images", ar.getImages());
						articleMap.put("keywords", ar.getKeywords());
						if (ar.getEventStartDate() != null) {
							articleMap.put("eventStartDate", ar.getEventStartDate());
						}
						if (ar.getEventEndDate() != null) {
							articleMap.put("eventEndDate", ar.getEventEndDate());
						}
					}
					
					return profileEnrichmentUtil.enrichArticleAndComments(articleMap, comments)
							.map(ac -> {
								// 단건 조회에서 nickname -> writerName으로 필드명 변환
								Map<String, Object> article = (Map<String, Object>) ac.get("article");
								if (article != null) {
									// nickname을 writerName으로, profileImageUrl을 writerProfileImage으로 변환
									Object nickname = article.remove("nickname");
									Object profileImageUrl = article.remove("profileImageUrl");
									if (nickname != null) {
										article.put("writerName", nickname);
									}
									if (profileImageUrl != null) {
										article.put("writerProfileImage", profileImageUrl);
									}
								}
								
								Map<String, Object> data = new LinkedHashMap<>(ac);
								data.put("likeDetail", likeDetail);
								return responseFactory.ok(data, req);
							});
				});
	}
	
	@Operation(summary = "일반 게시글 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ArticleListSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"page\": {\n      \"items\": [\n        {\n          \"articleId\": \"42840044-0f3e-482c-b5d5-0883af43e63e\",\n          \"title\": \"공연 함께 하실 분\",\n          \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n          \"writerId\": \"user_123\",\n          \"board\": { \"1\": \"공지사항\" },\n          \"imageUrls\": {},\n          \"keywords\": { \"10\": \"중요\" },\n          \"lastestUpdateId\": \"2025-10-11T17:52:27\",\n          \"commentCount\": 0,\n          \"likeCount\": 0\n        }\n      ],\n      \"nextCursorUpdatedAt\": \"2025-10-11T17:52:23\",\n      \"nextCursorId\": \"6ad747b9-0f34-48ad-8dba-5afa2f7b822f\",\n      \"hasNext\": false,\n      \"size\": 10\n    },\n    \"likeCounts\": [\n      {\n        \"referenceId\": \"42840044-0f3e-482c-b5d5-0883af43e63e\",\n        \"likeCount\": 0\n      }\n    ],\n    \"commentCounts\": {\n      \"42840044-0f3e-482c-b5d5-0883af43e63e\": 0\n    }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/regular?size=10\"\n  }\n}")))
	})
	@GetMapping("")
	public Mono<ResponseEntity<BaseResponse>> getArticles(
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String cursorId,
			@RequestParam(required = false) Long boardIds,
			@RequestParam(required = false) List<Long> keyword,
			@RequestParam(required = false) String title,
			@RequestParam(required = false) String content,
			@RequestParam(required = false) String writerId,
			ServerHttpRequest req) {
		return articleClient.fetchArticleCursorPageResponse(size, cursorId, boardIds, keyword, title, content, writerId)
				.flatMap(page -> {
					// Enrich ArticleResponse items with profile information first
					List<ArticleResponse> items = page.getItems() == null ? List.of() : page.getItems();
					
					// 디버깅: 원본 아이템 확인
					log.debug("[게시글 목록 조회] 원본 아이템 수: {}", items.size());
					if (!items.isEmpty()) {
						ArticleResponse first = items.get(0);
						log.info("[게시글 목록 조회] 첫 번째 게시글 - articleId: {}, writerId: {}, writerName: {}, writerProfileImage: {}",
								first.getArticleId(), first.getWriterId(), first.getWriterName(), first.getWriterProfileImage());
					}
					
					return profileEnrichmentUtil.enrichArticleResponseList(items)
							.doOnNext(enriched -> {
								// 디버깅: 프로필 보강 후 확인
								log.debug("[게시글 목록 조회] 프로필 보강 후 아이템 수: {}", enriched.size());
								if (!enriched.isEmpty()) {
									ArticleResponse first = enriched.get(0);
									log.info("[게시글 목록 조회] 프로필 보강 후 첫 번째 게시글 - articleId: {}, writerId: {}, writerName: {}, writerProfileImage: {}",
											first.getArticleId(), first.getWriterId(), first.getWriterName(), first.getWriterProfileImage());
								}
							})
							.flatMap(enrichedArticles -> {
								// Extract article IDs for fetching counts
								List<String> ids = enrichedArticles.stream()
										.map(ArticleResponse::getArticleId)
										.filter(Objects::nonNull)
										.toList();
								
								Mono<List<LikeCountResponse>> likeCountsMono = likeClient.getLikeCounts(categoryId, ids)
										.onErrorReturn(List.of());
								Mono<Map<String, Integer>> commentCountsMono = commentClient.getCountsForArticles(ids)
										.onErrorReturn(Map.of());
								
								return Mono.zip(likeCountsMono, commentCountsMono)
										.map(tuple2 -> {
											// Build quick lookup maps for counts
											Map<String, Integer> likeCountMap = tuple2.getT1() == null ? Map.of() : tuple2.getT1().stream()
													.filter(Objects::nonNull)
													.collect(Collectors.toMap(
															LikeCountResponse::getReferenceId,
															lc -> lc.getLikeCount() == null ? 0 : lc.getLikeCount()
													));
											Map<String, Integer> commentCountMap = tuple2.getT2() == null ? Map.of() : tuple2.getT2();
											
											// Convert enriched ArticleResponse to Map with counts
											List<Map<String, Object>> itemMaps = enrichedArticles.stream()
													.map(item -> {
														Map<String, Object> m = new LinkedHashMap<>();
														m.put("articleId", item.getArticleId());
														m.put("title", item.getTitle());
														m.put("content", item.getContent());
														m.put("writerId", item.getWriterId());
														m.put("writerName", item.getWriterName());
														m.put("writerProfileImage", item.getWriterProfileImage());
														m.put("board", item.getBoard());
														m.put("status", item.getStatus());
														m.put("viewCount", item.getViewCount());
														m.put("firstImageUrl", item.getFirstImageUrl());
														m.put("createdAt", item.getCreatedAt());
														m.put("updatedAt", item.getUpdatedAt());
														m.put("images", item.getImages());
														m.put("keywords", item.getKeywords());
														if (item.getEventStartDate() != null) {
															m.put("eventStartDate", item.getEventStartDate());
														}
														if (item.getEventEndDate() != null) {
															m.put("eventEndDate", item.getEventEndDate());
														}
														m.put("commentCount", commentCountMap.getOrDefault(item.getArticleId(), 0));
														m.put("likeCount", likeCountMap.getOrDefault(item.getArticleId(), 0));
														return m;
													})
													.toList();
											
											// Build page response
											java.util.Map<String, Object> pageMap = new java.util.LinkedHashMap<>();
											pageMap.put("items", itemMaps);
											pageMap.put("nextCursorUpdatedAt", page.getNextCursorUpdatedAt());
											pageMap.put("nextCursorId", page.getNextCursorId());
											pageMap.put("hasNext", page.isHasNext());
											pageMap.put("size", page.getSize());
											return responseFactory.ok(java.util.Map.of("page", pageMap), req);
										});
							});
				});
	}
	
	private String resolveCurrentUserId(ServerHttpRequest req) {
		if (req == null || req.getHeaders() == null) return null;
		String[] headerKeys = new String[]{
				"X-USER-ID", "X-User-Id", "X-USERID", "X-USER", "User-Id", "userId"
		};
		for (String k : headerKeys) {
			String v = req.getHeaders().getFirst(k);
			if (v != null && !v.isBlank()) return v;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private java.util.Map<String, Object> sanitizeCommentMap(java.util.Map<String, Object> c, String currentUserId) {
		java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
		if (c == null) return m;
		for (java.util.Map.Entry<String, Object> e : c.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			if ("referenceId".equals(key) || "articleId".equals(key)) {
				continue; // drop
			}
			if ("replies".equals(key) && value instanceof java.util.List<?> listVal) {
				java.util.List<java.util.Map<String, Object>> cleaned = new java.util.ArrayList<>();
				for (Object o : listVal) {
					if (o instanceof java.util.Map<?, ?> mm) {
						cleaned.add(sanitizeCommentMap((java.util.Map<String, Object>) mm, currentUserId));
					}
				}
				m.put("replies", cleaned);
			} else {
				m.put(key, value);
			}
		}
		// Add isOwn flag if we can determine writerId
		Object writerIdObj = m.get("writerId");
		boolean isOwn = currentUserId != null && writerIdObj != null && currentUserId.equals(String.valueOf(writerIdObj));
		m.put("isOwn", isOwn);
		return m;
	}
}
