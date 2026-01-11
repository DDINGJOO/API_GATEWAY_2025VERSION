package com.study.api_gateway.api.gaechu.controller;

import com.study.api_gateway.api.gaechu.service.GaechuFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.common.util.UserIdValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/gaechu")
@RequiredArgsConstructor
public class GaechuController implements GaechuApi {
	private final GaechuFacadeService gaechuFacadeService;
	private final ResponseFactory responseFactory;
	private final UserIdValidator userIdValidator;
	
	@Override
	@PostMapping("/likes/{categoryId}/{referenceId}")
	public Mono<ResponseEntity<BaseResponse>> likeOrUnlike(@PathVariable String categoryId,
	                                                       @PathVariable String referenceId,
	                                                       @RequestParam String likerId,
	                                                       @RequestParam boolean isLike,
	                                                       ServerHttpRequest request) {
		// 토큰의 userId와 요청의 likerId 검증 (다른 사람 대신 좋아요 방지)
		return userIdValidator.validateReactive(request, likerId)
				.then(gaechuFacadeService.likeOrUnlike(categoryId, referenceId, likerId, isLike))
				.thenReturn(responseFactory.ok(null, request, HttpStatus.NO_CONTENT));
	}
}
