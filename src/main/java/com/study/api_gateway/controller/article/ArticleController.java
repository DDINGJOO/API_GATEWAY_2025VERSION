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

@RestController
@RequestMapping("/bff/v1/communities/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {
	private final ArticleClient articleClient;
	private final CommentClient commentClient;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	
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
						examples = @ExampleObject(name = "ArticleDetailWithComments", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"article\": {\n      \"articleId\": \"article-1\",\n      \"title\": \"제목\"\n    },\n    \"comments\": [\n      { \"commentId\": \"c1\", \"contents\": \"첫 댓글\" }\n    ]\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/{articleId}\"\n  }\n}")))
	})
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getArticle(@PathVariable String articleId, ServerHttpRequest req) {
		return Mono.zip(
				articleClient.getArticle(articleId),
				commentClient.getCommentsByArticle(articleId)
		)
		.map(tuple -> {
			var article = tuple.getT1();
			var comments = tuple.getT2();
			return responseFactory.ok(Map.of(
					"article", article,
					"comments", comments
			), req);
		});
	}
	
	@Operation(summary = "게시글 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
						schema = @Schema(implementation = BaseResponse.class),
						examples = @ExampleObject(name = "ArticleListSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"items\": [ { \"articleId\": \"article-1\" } ],\n    \"nextCursor\": \"abc\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles\"\n  }\n}")))
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
				.map(result -> responseFactory.ok(result, req));
	}
}
