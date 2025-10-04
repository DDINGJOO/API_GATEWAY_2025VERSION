package com.study.api_gateway.controller.article;

import com.study.api_gateway.client.ArticleClient;
import com.study.api_gateway.dto.Article.request.ArticleCreateRequest;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.service.ImageConfirmService;
import com.study.api_gateway.util.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bff/v1/communities/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {
	private final ArticleClient articleClient;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	
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
	
	@DeleteMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> deleteArticle(@PathVariable String articleId, ServerHttpRequest req) {
		return articleClient.deleteArticle(articleId)
				.thenReturn(responseFactory.ok("deleted", req, HttpStatus.OK));
	}
	
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getArticle(@PathVariable String articleId, ServerHttpRequest req) {
		return articleClient.getArticle(articleId)
				.map(result -> responseFactory.ok(result, req));
	}
	
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
