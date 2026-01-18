package com.study.api_gateway.aggregation.articleDetail.controller;

import com.study.api_gateway.api.article.dto.request.ArticleCreateRequest;
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

import java.util.List;

/**
 * 일반 게시글 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Article", description = "일반 게시글 API")
public interface ArticleApi {
	
	@Operation(summary = "일반 게시글 생성")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> postArticle(
			@RequestBody ArticleCreateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "일반 게시글 수정")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PutMapping("/{articleId}")
	Mono<ResponseEntity<BaseResponse>> updateArticle(
			@PathVariable String articleId,
			@RequestBody ArticleCreateRequest request,
			ServerHttpRequest req);
	
	@Operation(summary = "일반 게시글 삭제")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@DeleteMapping("/{articleId}")
	Mono<ResponseEntity<BaseResponse>> deleteArticle(
			@PathVariable String articleId,
			ServerHttpRequest req);
	
	@Operation(summary = "일반 게시글 단건 조회(댓글 포함)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/{articleId}")
	Mono<ResponseEntity<BaseResponse>> getArticle(
			@PathVariable String articleId,
			ServerHttpRequest req);
	
	@Operation(summary = "일반 게시글 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping
	Mono<ResponseEntity<BaseResponse>> getArticles(
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String cursorId,
			@RequestParam(required = false) Long boardIds,
			@RequestParam(required = false) List<Long> keyword,
			@RequestParam(required = false) String title,
			@RequestParam(required = false) String content,
			@RequestParam(required = false) String writerId,
			ServerHttpRequest req);
}
