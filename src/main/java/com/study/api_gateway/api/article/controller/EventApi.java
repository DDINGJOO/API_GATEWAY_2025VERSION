package com.study.api_gateway.api.article.controller;

import com.study.api_gateway.api.article.dto.request.EventArticleCreateRequest;
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
 * 이벤트 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Event", description = "이벤트 관련 API")
public interface EventApi {
	
	@Operation(summary = "이벤트 생성")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> postEvent(
			@RequestBody EventArticleCreateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "이벤트 수정")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PutMapping("/{articleId}")
	Mono<ResponseEntity<BaseResponse>> updateEvent(
			@PathVariable String articleId,
			@RequestBody EventArticleCreateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "이벤트 삭제")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@DeleteMapping("/{articleId}")
	Mono<ResponseEntity<BaseResponse>> deleteEvent(
			@PathVariable String articleId,
			ServerHttpRequest req);
	
	@Operation(summary = "이벤트 단건 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/{articleId}")
	Mono<ResponseEntity<BaseResponse>> getEvent(
			@PathVariable String articleId,
			ServerHttpRequest req);
	
	@Operation(summary = "이벤트 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping
	Mono<ResponseEntity<BaseResponse>> getEvents(
			@RequestParam(required = false, defaultValue = "all") String status,
			@RequestParam(required = false, defaultValue = "0") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer size,
			ServerHttpRequest req);
}
