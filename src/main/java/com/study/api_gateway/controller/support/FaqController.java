package com.study.api_gateway.controller.support;

import com.study.api_gateway.client.FaqClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.support.faq.FaqCategory;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/support/faqs")
@RequiredArgsConstructor
public class FaqController {
	private final FaqClient faqClient;
	private final ResponseFactory responseFactory;
	
	@Operation(summary = "FAQ 목록 조회", description = "카테고리별 FAQ 목록을 조회합니다. category가 없으면 전체 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getFaqs(
			@RequestParam(required = false) FaqCategory category,
			ServerHttpRequest req) {
		return faqClient.getFaqs(category)
				.collectList()
				.map(list -> responseFactory.ok(list, req));
	}
}
