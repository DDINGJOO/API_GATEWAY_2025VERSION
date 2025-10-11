package com.study.api_gateway.controller.article;

import com.study.api_gateway.client.ArticleClient;
import com.study.api_gateway.client.CommentClient;
import com.study.api_gateway.dto.Article.request.ArticleCreateRequest;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.service.ImageConfirmService;
import com.study.api_gateway.util.ResponseFactory;
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
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/bff/v1/communities/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {
	private final ArticleClient articleClient;
	private final CommentClient commentClient;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	private final com.study.api_gateway.client.LikeClient likeClient;
	
	private final String categoryId = "ARTICLE";
	
	@Operation(summary = "게시글 생성")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
						schema = @Schema(implementation = BaseResponse.class),
						examples = @ExampleObject(name = "ArticleCreateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"article-1\",\n    \"title\": \"제목\",\n    \"content\": \"내용\",\n    \"imageUrls\": [\"img-1\"]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles\"\n  }\n}")))
	})
	@PostMapping()
	public Mono<ResponseEntity<BaseResponse>> postArticle(@RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		return articleClient.postArticle(request)
				.flatMap(result -> {
					List<String> imageIds = request.getImageUrls();
					if (imageIds != null && !imageIds.isEmpty() && result.getArticleId() != null) {
						return imageConfirmService.confirmImage(result.getArticleId(), imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Operation(summary = "게시글 수정")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
						schema = @Schema(implementation = BaseResponse.class),
						examples = @ExampleObject(name = "ArticleUpdateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"article-1\",\n    \"title\": \"수정된 제목\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/{articleId}\"\n  }\n}")))
	})
	@PutMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> updateArticle(@PathVariable String articleId, @RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		return articleClient.updateArticle(articleId, request)
				.flatMap(result -> {
					List<String> imageIds = request.getImageUrls();
					if (imageIds != null && !imageIds.isEmpty()) {
						return imageConfirmService.confirmImage(articleId, imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Operation(summary = "게시글 삭제")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
						schema = @Schema(implementation = BaseResponse.class),
						examples = @ExampleObject(name = "ArticleDeleteSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"deleted\",\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/{articleId}\"\n  }\n}")))
	})
	@DeleteMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> deleteArticle(@PathVariable String articleId, ServerHttpRequest req) {
		return articleClient.deleteArticle(articleId)
				.thenReturn(responseFactory.ok("deleted", req, HttpStatus.OK));
	}
	
	@Operation(summary = "게시글 단건 조회(댓글 포함)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
						schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ArticleDetailWithComments", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"article\": {\n      \"articleId\": \"42840044-0f3e-482c-b5d5-0883af43e63e\",\n      \"title\": \"공연 함께 하실 분\",\n      \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n      \"writerId\": \"user_123\",\n      \"board\": { \"1\": \"공지사항\" },\n      \"imageUrls\": {},\n      \"keywords\": { \"10\": \"중요\" },\n      \"lastestUpdateId\": \"2025-10-11T17:52:27\"\n    },\n    \"comments\": [\n      { \"commentId\": \"c1\", \"writerId\": \"user_123\", \"contents\": \"첫 댓글\", \"isOwn\": true, \"replies\": [] }\n    ],\n    \"likeDetail\": { \"likeCount\": 0, \"isOwn\": false }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/{articleId}\"\n  }\n}")))
	})
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getArticle(@PathVariable String articleId, ServerHttpRequest req) {
		return Mono.zip(
						articleClient.getArticle(articleId),
						commentClient.getCommentsByArticle(articleId, 0, 10, "visibleCount"),
						likeClient.getLikeDetail(categoryId, articleId)
				)
				.map(tuple3 -> {
					// Try to resolve current userId from headers (placeholder until token parsing is added)
					String currentUserId = resolveCurrentUserId(req);
					
					// Build likeDetail without referenceId and likerIds; add isOwn if current user liked
					com.study.api_gateway.dto.gaechu.LikeDetailResponse ld = tuple3.getT3();
					java.util.Map<String, Object> likeDetail = new java.util.LinkedHashMap<>();
					int likeCount = ld == null || ld.getLikeCount() == null ? 0 : ld.getLikeCount();
					likeDetail.put("likeCount", likeCount);
					boolean isOwnLike = false;
					if (currentUserId != null && ld != null && ld.getLikerIds() != null) {
						isOwnLike = ld.getLikerIds().stream().filter(java.util.Objects::nonNull).anyMatch(currentUserId::equals);
					}
					likeDetail.put("isOwn", isOwnLike);
					
					// Sanitize comments: remove keys referenceId and articleId; add isOwn if writerId == currentUserId; handle nested replies
					java.util.List<java.util.Map<String, Object>> rawComments = tuple3.getT2();
					java.util.List<java.util.Map<String, Object>> comments = rawComments == null ? java.util.List.of() : rawComments.stream()
							.map(c -> sanitizeCommentMap(c, currentUserId))
							.toList();
					
					return responseFactory.ok(java.util.Map.of(
						"article", tuple3.getT1(),
							"comments", comments,
							"likeDetail", likeDetail
					), req);
				});
	}
	
	@Operation(summary = "게시글 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
						schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "ArticleListSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"page\": {\n      \"items\": [\n        {\n          \"articleId\": \"42840044-0f3e-482c-b5d5-0883af43e63e\",\n          \"title\": \"공연 함께 하실 분\",\n          \"content\": \"같이 즐겁게 공연하실 분을 찾습니다.\",\n          \"writerId\": \"user_123\",\n          \"board\": { \"1\": \"공지사항\" },\n          \"imageUrls\": {},\n          \"keywords\": { \"10\": \"중요\" },\n          \"lastestUpdateId\": \"2025-10-11T17:52:27\",\n          \"commentCount\": 0,\n          \"likeCount\": 0\n        }\n      ],\n      \"nextCursorUpdatedAt\": \"2025-10-11T17:52:23\",\n      \"nextCursorId\": \"6ad747b9-0f34-48ad-8dba-5afa2f7b822f\",\n      \"hasNext\": false,\n      \"size\": 10\n    },\n    \"likeCounts\": [\n      {\n        \"referenceId\": \"42840044-0f3e-482c-b5d5-0883af43e63e\",\n        \"likeCount\": 0\n      }\n    ],\n    \"commentCounts\": {\n      \"42840044-0f3e-482c-b5d5-0883af43e63e\": 0\n    }\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles?size=10\"\n  }\n}")))
	})
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getArticles(      @RequestParam(required = false) Integer size,
	                                                                         @RequestParam(required = false) String cursorId,
	                                                                         @RequestParam(required = false) Object board,
	                                                                         @RequestParam(required = false) List<?> keyword,
	                                                                         @RequestParam(required = false) String title,
	                                                                         @RequestParam(required = false) String content,
	                                                                         @RequestParam(required = false, name = "writerIds") List<String> writerIds,
	                                                                         ServerHttpRequest req)
	{
		return articleClient.fetchArticleCursorPageResponse(size, cursorId, board, keyword, title, content, writerIds)
				.flatMap(page -> {
					List<String> ids = page.getItems() == null ? List.of() : page.getItems().stream()
							.map(com.study.api_gateway.dto.Article.response.ArticleResponse::getArticleId)
							.filter(Objects::nonNull)
							.toList();
					Mono<List<com.study.api_gateway.dto.gaechu.LikeCountResponse>> likeCountsMono = likeClient.getLikeCounts(categoryId, ids);
					Mono<Map<String, Integer>> commentCountsMono = commentClient.getCountsForArticles(ids);
					return Mono.zip(likeCountsMono, commentCountsMono)
							.map(tuple2 -> {
								// Build quick lookup maps for counts
								Map<String, Integer> likeCountMap = tuple2.getT1() == null ? Map.of() : tuple2.getT1().stream()
										.filter(Objects::nonNull)
										.collect(java.util.stream.Collectors.toMap(
												com.study.api_gateway.dto.gaechu.LikeCountResponse::getReferenceId,
												lc -> lc.getLikeCount() == null ? 0 : lc.getLikeCount()
										));
								Map<String, Integer> commentCountMap = tuple2.getT2() == null ? Map.of() : tuple2.getT2();
								
								// Enrich items by embedding counts
								java.util.List<java.util.Map<String, Object>> enrichedItems = page.getItems() == null ? java.util.List.of() : page.getItems().stream()
										.map(item -> {
											java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
											m.put("articleId", item.getArticleId());
											m.put("title", item.getTitle());
											m.put("content", item.getContent());
											m.put("writerId", item.getWriterId());
											m.put("board", item.getBoard());
											m.put("imageUrls", item.getImageUrls());
											m.put("keywords", item.getKeywords());
											m.put("lastestUpdateId", item.getLastestUpdateId());
											m.put("commentCount", commentCountMap.getOrDefault(item.getArticleId(), 0));
											m.put("likeCount", likeCountMap.getOrDefault(item.getArticleId(), 0));
											return m;
										})
										.toList();
								
								java.util.Map<String, Object> pageMap = new java.util.LinkedHashMap<>();
								pageMap.put("items", enrichedItems);
								pageMap.put("nextCursorUpdatedAt", page.getNextCursorUpdatedAt());
								pageMap.put("nextCursorId", page.getNextCursorId());
								pageMap.put("hasNext", page.isHasNext());
								pageMap.put("size", page.getSize());
								
								// Keep original aggregated counts for backward compatibility
								return responseFactory.ok(java.util.Map.of(
										"page", pageMap
								), req);
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
