package com.study.api_gateway.domain.like;

import com.study.api_gateway.common.dto.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/like")
@RequiredArgsConstructor
public class LikeController {
	private final LikeClient likeClient;
	private final ResponseFactory responseFactory;
	
	@Operation(summary = "좋아요/좋아요 취소",
			description = "지정된 카테고리(categoryId)와 대상(referenceId)에 대해 특정 사용자의 좋아요 상태를 토글합니다.\n" +
					"- isLike=true: 좋아요 추가\n" +
					"- isLike=false: 좋아요 취소\n" +
					"예) 게시글(ARTICLE) art_001에 대해 user_99가 좋아요를 누르거나 취소")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = {
									@ExampleObject(name = "LikeSuccess", summary = "좋아요 ON",
											description = "ARTICLE 카테고리의 art_001을 user_99가 좋아요로 설정(isLike=true)",
											value = "{\n  \"isSuccess\": true,\n  \"code\": 204,\n  \"data\": null,\n  \"request\": {\n    \"path\": \"/bff/v1/gaechu/likes/ARTICLE/art_001?likerId=user_99&isLike=true\",\n    \"url\": \"https://api.bander.app/bff/v1/gaechu/likes/ARTICLE/art_001?likerId=user_99&isLike=true\"\n  }\n}"),
									@ExampleObject(name = "UnlikeSuccess", summary = "좋아요 OFF",
											description = "ARTICLE 카테고리의 art_001에 대해 user_99가 좋아요를 취소(isLike=false)",
											value = "{\n  \"isSuccess\": true,\n  \"code\": 204,\n  \"data\": null,\n  \"request\": {\n    \"path\": \"/bff/v1/gaechu/likes/ARTICLE/art_001?likerId=user_99&isLike=false\",\n    \"url\": \"https://api.bander.app/bff/v1/gaechu/likes/ARTICLE/art_001?likerId=user_99&isLike=false\"\n  }\n}")
							}
					))
	})
	@PostMapping("/likes/{categoryId}/{referenceId}")
	public Mono<ResponseEntity<BaseResponse>> likeOrUnlike(@PathVariable String categoryId,
	                                                       @PathVariable String referenceId,
	                                                       @RequestParam String likerId,
	                                                       @RequestParam boolean isLike,
	                                                       ServerHttpRequest request) {
		return likeClient.likeOrUnlike(categoryId, referenceId, likerId, isLike)
				.thenReturn(responseFactory.ok(null, request, HttpStatus.NO_CONTENT));
	}
}
