package com.study.api_gateway.controller.gaechu;

import com.study.api_gateway.client.LikeClient;
import com.study.api_gateway.dto.BaseResponse;
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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/gaechu")
@RequiredArgsConstructor
public class GaechuController {
	private final LikeClient likeClient;
	private final ResponseFactory responseFactory;
	
	@Operation(summary = "좋아요/좋아요 취소")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "LikeOrUnlike", value = "{\n  \"isSuccess\": true,\n  \"code\": 204,\n  \"data\": null,\n  \"request\": { \"path\": \"/bff/v1/gaechu/likes/{categoryId}/{referenceId}\" }\n}")))
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
