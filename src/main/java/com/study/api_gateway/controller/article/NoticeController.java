package com.study.api_gateway.controller.article;

import com.study.api_gateway.client.NoticeClient;
import com.study.api_gateway.dto.Article.request.ArticleCreateRequest;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.service.ImageConfirmService;
import com.study.api_gateway.util.ProfileEnrichmentUtil;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/v1/communities/articles/notices")
@RequiredArgsConstructor
@Validated
public class NoticeController {
	private final NoticeClient noticeClient;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	
	@Operation(summary = "공지사항 생성")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "NoticeCreateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"notice-1\",\n    \"title\": \"중요 공지\",\n    \"content\": \"공지사항입니다.\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/notices\"\n  }\n}")))
	})
	@PostMapping()
	public Mono<ResponseEntity<BaseResponse>> postNotice(@RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		return noticeClient.postNotice(request)
				.flatMap(result -> {
					List<String> imageIds = request.getImageUrls();
					if (imageIds != null && !imageIds.isEmpty() && result.getArticleId() != null) {
						return imageConfirmService.confirmImage(result.getArticleId(), imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Operation(summary = "공지사항 수정")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "NoticeUpdateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"notice-1\",\n    \"title\": \"수정된 공지\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/notices/{articleId}\"\n  }\n}")))
	})
	@PutMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> updateNotice(@PathVariable String articleId, @RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		return noticeClient.updateNotice(articleId, request)
				.flatMap(result -> {
					List<String> imageIds = request.getImageUrls();
					if (imageIds != null && !imageIds.isEmpty()) {
						return imageConfirmService.confirmImage(articleId, imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Operation(summary = "공지사항 삭제")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "NoticeDeleteSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"deleted\",\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/notices/{articleId}\"\n  }\n}")))
	})
	@DeleteMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> deleteNotice(@PathVariable String articleId, ServerHttpRequest req) {
		return noticeClient.deleteNotice(articleId)
				.thenReturn(responseFactory.ok("deleted", req, HttpStatus.OK));
	}
	
	@Operation(summary = "공지사항 단건 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "NoticeDetail", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"notice-1\",\n    \"title\": \"중요 공지\",\n    \"content\": \"공지사항입니다.\",\n    \"writerId\": \"admin_123\",\n    \"board\": { \"1\": \"NOTICE\" },\n    \"imageUrls\": {},\n    \"keywords\": { \"10\": \"중요\" },\n    \"lastestUpdateId\": \"2025-10-15T10:00:00\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/notices/{articleId}\"\n  }\n}")))
	})
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getNotice(@PathVariable String articleId, ServerHttpRequest req) {
		return noticeClient.getNotice(articleId)
				.flatMap(article -> {
					Map<String, Object> articleMap = new LinkedHashMap<>();
					if (article != null) {
						articleMap.put("articleId", article.getArticleId());
						articleMap.put("title", article.getTitle());
						articleMap.put("content", article.getContent());
						articleMap.put("writerId", article.getWriterId());
						articleMap.put("board", article.getBoard());
						articleMap.put("imageUrls", article.getImageUrls());
						articleMap.put("keywords", article.getKeywords());
						articleMap.put("lastestUpdateId", article.getLastestUpdateId());
					}
					
					return profileEnrichmentUtil.enrichArticle(articleMap)
							.map(enrichedArticle -> responseFactory.ok(enrichedArticle, req));
				});
	}
	
	@Operation(summary = "공지사항 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "NoticeListSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"content\": [\n      {\n        \"articleId\": \"notice-1\",\n        \"title\": \"중요 공지\",\n        \"content\": \"공지사항입니다.\"\n      }\n    ],\n    \"number\": 0,\n    \"size\": 10,\n    \"totalElements\": 1,\n    \"totalPages\": 1\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/notices?page=0&size=10\"\n  }\n}")))
	})
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getNotices(@RequestParam(required = false, defaultValue = "0") Integer page,
	                                                     @RequestParam(required = false, defaultValue = "10") Integer size,
	                                                     ServerHttpRequest req) {
		return noticeClient.getNotices(page, size)
				.map(pageResponse -> responseFactory.ok(pageResponse, req));
	}
}
