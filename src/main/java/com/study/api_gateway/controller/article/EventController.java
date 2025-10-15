package com.study.api_gateway.controller.article;

import com.study.api_gateway.client.EventClient;
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
@RequestMapping("/bff/v1/communities/articles/events")
@RequiredArgsConstructor
@Validated
public class EventController {
	private final EventClient eventClient;
	private final ImageConfirmService imageConfirmService;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	
	@Operation(summary = "이벤트 생성")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "EventCreateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"event-1\",\n    \"title\": \"밴드 페스티벌\",\n    \"content\": \"연말 페스티벌입니다.\",\n    \"eventStartDate\": \"2025-12-24T18:00:00\",\n    \"eventEndDate\": \"2025-12-26T23:00:00\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/events\"\n  }\n}")))
	})
	@PostMapping()
	public Mono<ResponseEntity<BaseResponse>> postEvent(@RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		return eventClient.postEvent(request)
				.flatMap(result -> {
					List<String> imageIds = request.getImageUrls();
					if (imageIds != null && !imageIds.isEmpty() && result.getArticleId() != null) {
						return imageConfirmService.confirmImage(result.getArticleId(), imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Operation(summary = "이벤트 수정")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "EventUpdateSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"event-1\",\n    \"title\": \"수정된 이벤트\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/events/{articleId}\"\n  }\n}")))
	})
	@PutMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> updateEvent(@PathVariable String articleId, @RequestBody ArticleCreateRequest request, ServerHttpRequest req) {
		return eventClient.updateEvent(articleId, request)
				.flatMap(result -> {
					List<String> imageIds = request.getImageUrls();
					if (imageIds != null && !imageIds.isEmpty()) {
						return imageConfirmService.confirmImage(articleId, imageIds)
								.thenReturn(responseFactory.ok(result, req));
					}
					return Mono.just(responseFactory.ok(result, req));
				});
	}
	
	@Operation(summary = "이벤트 삭제")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "EventDeleteSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": \"deleted\",\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/events/{articleId}\"\n  }\n}")))
	})
	@DeleteMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> deleteEvent(@PathVariable String articleId, ServerHttpRequest req) {
		return eventClient.deleteEvent(articleId)
				.thenReturn(responseFactory.ok("deleted", req, HttpStatus.OK));
	}
	
	@Operation(summary = "이벤트 단건 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "EventDetail", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"articleId\": \"event-1\",\n    \"title\": \"밴드 페스티벌\",\n    \"content\": \"연말 페스티벌입니다.\",\n    \"writerId\": \"admin_123\",\n    \"board\": { \"3\": \"EVENT\" },\n    \"imageUrls\": {},\n    \"keywords\": { \"10\": \"MUSIC\" },\n    \"lastestUpdateId\": \"2025-10-15T10:00:00\",\n    \"eventStartDate\": \"2025-12-24T18:00:00\",\n    \"eventEndDate\": \"2025-12-26T23:00:00\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/events/{articleId}\"\n  }\n}")))
	})
	@GetMapping("/{articleId}")
	public Mono<ResponseEntity<BaseResponse>> getEvent(@PathVariable String articleId, ServerHttpRequest req) {
		return eventClient.getEvent(articleId)
				.flatMap(event -> {
					Map<String, Object> eventMap = new LinkedHashMap<>();
					if (event != null) {
						eventMap.put("articleId", event.getArticleId());
						eventMap.put("title", event.getTitle());
						eventMap.put("content", event.getContent());
						eventMap.put("writerId", event.getWriterId());
						eventMap.put("board", event.getBoard());
						eventMap.put("imageUrls", event.getImageUrls());
						eventMap.put("keywords", event.getKeywords());
						eventMap.put("lastestUpdateId", event.getLastestUpdateId());
						eventMap.put("eventStartDate", event.getEventStartDate());
						eventMap.put("eventEndDate", event.getEventEndDate());
					}
					
					return profileEnrichmentUtil.enrichArticle(eventMap)
							.map(enrichedEvent -> responseFactory.ok(enrichedEvent, req));
				});
	}
	
	@Operation(summary = "이벤트 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "EventListSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"content\": [\n      {\n        \"articleId\": \"event-1\",\n        \"title\": \"밴드 페스티벌\",\n        \"content\": \"연말 페스티벌입니다.\",\n        \"eventStartDate\": \"2025-12-24T18:00:00\",\n        \"eventEndDate\": \"2025-12-26T23:00:00\"\n      }\n    ],\n    \"number\": 0,\n    \"size\": 10,\n    \"totalElements\": 1,\n    \"totalPages\": 1\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/articles/events?status=all&page=0&size=10\"\n  }\n}")))
	})
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getEvents(@RequestParam(required = false, defaultValue = "all") String status,
	                                                    @RequestParam(required = false, defaultValue = "0") Integer page,
	                                                    @RequestParam(required = false, defaultValue = "10") Integer size,
	                                                    ServerHttpRequest req) {
		return eventClient.getEvents(status, page, size)
				.map(pageResponse -> responseFactory.ok(pageResponse, req));
	}
}
