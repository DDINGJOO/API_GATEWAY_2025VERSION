package com.study.api_gateway.controller.article;

import com.study.api_gateway.client.ArticleClient;
import com.study.api_gateway.dto.Article.request.ArticleCreateRequest;
import com.study.api_gateway.dto.Article.response.ArticleCursorPageResponse;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.util.ResponseFactory;
import lombok.RequiredArgsConstructor;
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
	private final ResponseFactory responseFactory;
	
	@PostMapping()
	public Mono<ResponseEntity<BaseResponse>> postArticle(@RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		return articleClient.postArticle(request)
				.map(result -> responseFactory.ok(result, req));
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
