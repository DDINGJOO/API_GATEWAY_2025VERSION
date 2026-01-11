package com.study.api_gateway.api.comment.controller;

import com.study.api_gateway.api.comment.dto.request.CombinedCommentCreateRequest;
import com.study.api_gateway.api.comment.dto.request.CommentUpdateRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 댓글 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Comment", description = "댓글 관련 API")
public interface CommentApi {

	@Operation(summary = "루트/대댓글 통합 생성",
			description = "parentId 파라미터가 없으면 루트 댓글을, 있으면 해당 부모에 대한 대댓글을 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성됨",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@PostMapping("/create")
	Mono<ResponseEntity<BaseResponse>> createCombined(
			@RequestParam(required = false) String parentId,
			@RequestBody CombinedCommentCreateRequest request,
			ServerHttpRequest req);

	@Operation(summary = "특정 아티클의 전체 댓글 조회(10개씩)",
			description = "특정 아티클의 전체 댓글을 조회합니다. mode=all: 전체 댓글 조회, mode=visibleCount: 페이징 처리")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	@GetMapping("/article")
	Mono<ResponseEntity<BaseResponse>> getByArticle(
			@RequestParam String articleId,
			@RequestParam(required = false, defaultValue = "0") Integer page,
			@RequestParam(required = false, defaultValue = "visibleCount") String mode,
			ServerHttpRequest req);

	@Operation(summary = "댓글 내용 수정",
			description = "작성자 본인만 수정 가능. 내용이 비어있으면 400 반환.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "403", description = "본인 아님"),
			@ApiResponse(responseCode = "400", description = "내용 비어있음"),
			@ApiResponse(responseCode = "404", description = "미존재")
	})
	@PatchMapping("/{id}")
	Mono<ResponseEntity<BaseResponse>> update(
			@PathVariable String id,
			@RequestBody CommentUpdateRequest request,
			ServerHttpRequest req);

	@Operation(summary = "댓글 소프트 삭제",
			description = "작성자 본인만 가능. 성공 시 204(No Content)와 함께 data는 null입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제됨",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class))),
			@ApiResponse(responseCode = "403", description = "본인 아님"),
			@ApiResponse(responseCode = "404", description = "미존재")
	})
	@DeleteMapping("/{id}")
	Mono<ResponseEntity<BaseResponse>> softDelete(
			@PathVariable String id,
			ServerHttpRequest req);
}
