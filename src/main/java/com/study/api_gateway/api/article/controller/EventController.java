package com.study.api_gateway.api.article.controller;

import com.study.api_gateway.api.article.dto.request.EventArticleCreateRequest;
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
@RequestMapping("/bff/v1/communities/articles/events")
@RequiredArgsConstructor
@Validated
public class EventController implements EventApi {
	private final ArticleFacadeService articleFacadeService;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final UserIdValidator userIdValidator;
	
	@Override
	@PostMapping()
	public Mono<ResponseEntity<BaseResponse>> postEvent(@RequestBody EventArticleCreateRequest request, ServerHttpRequest req) {
		// 토큰의 userId와 request의 writerId 검증
		return userIdValidator.validateReactive(req, request.getWriterId())
				.then(articleFacadeService.postEvent(request))
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
	public Mono<ResponseEntity<BaseResponse>> updateEvent(@PathVariable String articleId, @RequestBody EventArticleCreateRequest request, ServerHttpRequest req) {
		// 1. 토큰의 userId와 request의 writerId 검증
		return userIdValidator.validateReactive(req, request.getWriterId())
				// 2. Event 조회하여 실제 작성자 확인
				.then(articleFacadeService.getEvent(articleId))
				.flatMap(event -> userIdValidator.validateOwnership(req, event.getWriterId(), "이벤트"))
				// 3. 검증 통과 후 수정 진행
				.then(articleFacadeService.updateEvent(articleId, request))
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
	public Mono<ResponseEntity<BaseResponse>> deleteEvent(@PathVariable String articleId, ServerHttpRequest req) {
		// 1. Event 조회하여 실제 작성자 확인
		return articleFacadeService.getEvent(articleId)
				.flatMap(event -> userIdValidator.validateOwnership(req, event.getWriterId(), "이벤트"))
				// 2. 검증 통과 후 삭제 진행
				.then(articleFacadeService.deleteEvent(articleId))
				.thenReturn(responseFactory.ok("deleted", req, HttpStatus.OK));
	}
	
	@Override
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getEvent(@PathVariable String articleId, ServerHttpRequest req) {
		return articleFacadeService.getEvent(articleId)
				.flatMap(event -> {
					Map<String, Object> eventMap = new LinkedHashMap<>();
					if (event != null) {
						eventMap.put("articleId", event.getArticleId());
						eventMap.put("title", event.getTitle());
						eventMap.put("content", event.getContent());
						eventMap.put("writerId", event.getWriterId());
						eventMap.put("board", event.getBoard());
						eventMap.put("status", event.getStatus());
						eventMap.put("viewCount", event.getViewCount());
						eventMap.put("firstImageUrl", event.getFirstImageUrl());
						eventMap.put("createdAt", event.getCreatedAt());
						eventMap.put("updatedAt", event.getUpdatedAt());
						eventMap.put("images", event.getImages());
						eventMap.put("keywords", event.getKeywords());
						eventMap.put("eventStartDate", event.getEventStartDate());
						eventMap.put("eventEndDate", event.getEventEndDate());
					}
					
					return profileEnrichmentUtil.enrichArticle(eventMap)
							.map(enrichedEvent -> responseFactory.ok(enrichedEvent, req));
				});
	}
	
	@Override
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getEvents(@RequestParam(required = false, defaultValue = "all") String status,
	                                                    @RequestParam(required = false, defaultValue = "0") Integer page,
	                                                    @RequestParam(required = false, defaultValue = "10") Integer size,
	                                                    ServerHttpRequest req) {
		return articleFacadeService.getEvents(status, page, size)
				.map(pageResponse -> responseFactory.ok(pageResponse, req));
	}
}
