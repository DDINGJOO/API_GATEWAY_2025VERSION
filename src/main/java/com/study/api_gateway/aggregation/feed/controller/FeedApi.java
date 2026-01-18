package com.study.api_gateway.aggregation.feed.controller;

import com.study.api_gateway.api.activity.dto.request.FeedTotalsRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 피드 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Feed", description = "피드 활동 API")
public interface FeedApi {
	
	@Operation(summary = "피드 활동 총합 조회",
			description = "특정 사용자의 카테고리별 활동 총합(article, comment, like)과 조회자와 대상의 동일 여부를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> getFeedTotals(
			@RequestBody FeedTotalsRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "내 피드 활동 총합 조회",
			description = "로그인한 사용자 본인의 카테고리별 활동 총합을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@GetMapping("/me/totals")
	Mono<ResponseEntity<BaseResponse>> getMyFeedTotals(ServerHttpRequest req);
	
	@Operation(summary = "카테고리별 피드 조회",
			description = "지정된 카테고리(article, comment, like)에 대해 articleId 목록을 페이징으로 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@GetMapping("/{category}")
	Mono<ResponseEntity<BaseResponse>> getFeedByCategory(
			@PathVariable String category,
			@RequestParam String targetUserId,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false, defaultValue = "20") Integer size,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			ServerHttpRequest req);
	
	@Operation(summary = "내 피드 카테고리별 조회",
			description = "로그인한 사용자 본인의 피드를 카테고리별로 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@GetMapping("/me/{category}")
	Mono<ResponseEntity<BaseResponse>> getMyFeedByCategory(
			@PathVariable String category,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false, defaultValue = "20") Integer size,
			@RequestParam(required = false, defaultValue = "newest") String sort,
			ServerHttpRequest req);
}
