package com.study.api_gateway.api.gaechu.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * 좋아요(개추) API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Gaechu", description = "좋아요 API")
public interface GaechuApi {
	
	@Operation(summary = "좋아요/좋아요 취소",
			description = "지정된 카테고리(categoryId)와 대상(referenceId)에 대해 특정 사용자의 좋아요 상태를 토글합니다.\n" +
					"- isLike=true: 좋아요 추가\n" +
					"- isLike=false: 좋아요 취소")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = {
									@ExampleObject(name = "LikeSuccess", summary = "좋아요 ON",
											description = "좋아요 설정 성공",
											value = "{\"isSuccess\": true, \"code\": 204, \"data\": null}"),
									@ExampleObject(name = "UnlikeSuccess", summary = "좋아요 OFF",
											description = "좋아요 취소 성공",
											value = "{\"isSuccess\": true, \"code\": 204, \"data\": null}")
							}))
	})
	@PostMapping("/likes/{categoryId}/{referenceId}")
	Mono<ResponseEntity<BaseResponse>> likeOrUnlike(
			@PathVariable String categoryId,
			@PathVariable String referenceId,
			@RequestParam String likerId,
			@RequestParam boolean isLike,
			ServerHttpRequest request);
}
