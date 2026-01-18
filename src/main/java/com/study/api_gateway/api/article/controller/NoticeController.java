package com.study.api_gateway.api.article.controller;

import com.study.api_gateway.api.article.dto.request.ArticleCreateRequest;
import com.study.api_gateway.api.article.service.ArticleFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.common.util.UserIdValidator;
import com.study.api_gateway.enrichment.ImageConfirmService;
import com.study.api_gateway.enrichment.ProfileEnrichmentUtil;
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
public class NoticeController implements NoticeApi {
	private final ArticleFacadeService articleFacadeService;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final UserIdValidator userIdValidator;
	
	@Override
	@PostMapping()
	public Mono<ResponseEntity<BaseResponse>> postNotice(@RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		// 토큰의 userId와 request의 writerId 검증 (관리자 권한 확인은 서비스 레이어에서)
		return userIdValidator.validateReactive(req, request.getWriterId())
				.then(articleFacadeService.postNotice(request))
				.flatMap(result -> {
					List<String> imageIds = request.getImageIds();
					if (imageIds != null && !imageIds.isEmpty() && result.getArticleId() != null) {
						return imageConfirmService.confirmImage(result.getArticleId(), imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Override
	@PutMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> updateNotice(@PathVariable String articleId, @RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		// 1. 토큰의 userId와 request의 writerId 검증
		return userIdValidator.validateReactive(req, request.getWriterId())
				// 2. Notice 조회하여 실제 작성자 확인
				.then(articleFacadeService.getNotice(articleId))
				.flatMap(notice -> userIdValidator.validateOwnership(req, notice.getWriterId(), "공지사항"))
				// 3. 검증 통과 후 수정 진행
				.then(articleFacadeService.updateNotice(articleId, request))
				.flatMap(result -> {
					List<String> imageIds = request.getImageIds();
					if (imageIds != null && !imageIds.isEmpty()) {
						return imageConfirmService.confirmImage(articleId, imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Override
	@DeleteMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> deleteNotice(@PathVariable String articleId, ServerHttpRequest req) {
		// 1. Notice 조회하여 실제 작성자 확인
		return articleFacadeService.getNotice(articleId)
				.flatMap(notice -> userIdValidator.validateOwnership(req, notice.getWriterId(), "공지사항"))
				// 2. 검증 통과 후 삭제 진행
				.then(articleFacadeService.deleteNotice(articleId))
				.thenReturn(responseFactory.ok("deleted", req, HttpStatus.OK));
	}
	
	@Override
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getNotice(@PathVariable String articleId, ServerHttpRequest req) {
		return articleFacadeService.getNotice(articleId)
				.flatMap(article -> {
					Map<String, Object> articleMap = new LinkedHashMap<>();
					if (article != null) {
						articleMap.put("articleId", article.getArticleId());
						articleMap.put("title", article.getTitle());
						articleMap.put("content", article.getContent());
						articleMap.put("writerId", article.getWriterId());
						articleMap.put("board", article.getBoard());
						articleMap.put("status", article.getStatus());
						articleMap.put("viewCount", article.getViewCount());
						articleMap.put("firstImageUrl", article.getFirstImageUrl());
						articleMap.put("createdAt", article.getCreatedAt());
						articleMap.put("updatedAt", article.getUpdatedAt());
						articleMap.put("images", article.getImages());
						articleMap.put("keywords", article.getKeywords());
					}
					
					return profileEnrichmentUtil.enrichArticle(articleMap)
							.map(enrichedArticle -> responseFactory.ok(enrichedArticle, req));
				});
	}
	
	@Override
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getNotices(@RequestParam(required = false, defaultValue = "0") Integer page,
	                                                     @RequestParam(required = false, defaultValue = "10") Integer size,
	                                                     ServerHttpRequest req) {
		return articleFacadeService.getNotices(page, size)
				.map(pageResponse -> responseFactory.ok(pageResponse, req));
	}
}
