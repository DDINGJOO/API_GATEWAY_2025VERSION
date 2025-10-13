package com.study.api_gateway.controller.activity;

import com.study.api_gateway.client.ActivityClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.activity.request.FeedTotalsRequest;
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

@RestController
@RequestMapping("/bff/v1/activities/feed")
@RequiredArgsConstructor
@Validated
public class FeedController {
	private final ActivityClient activityClient;
	private final ResponseFactory responseFactory;
	
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
		
		// Check permission for 'like' category
		if ("like".equals(category)) {
			if (viewerId == null || viewerId.isBlank() || !viewerId.equals(targetUserId)) {
				return Mono.just(responseFactory.error("Access denied: like category can only be accessed by the owner",
						HttpStatus.FORBIDDEN, req));
			}
		}
		
		return activityClient.getFeedByCategory(category, viewerId, targetUserId, cursor, size, sort)
				.map(response -> responseFactory.ok(response, req))
				.onErrorResume(e ->
						Mono.just(responseFactory.error("Failed to fetch feed: " + e.getMessage(),
								HttpStatus.INTERNAL_SERVER_ERROR, req))
				);
	}
}
