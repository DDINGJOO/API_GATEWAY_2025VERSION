package com.study.api_gateway.controller.pricing;

import com.study.api_gateway.client.YeYakHaeYoClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 클라이언트 앱용 가격 정책 조회 API
 * RESTful 방식의 조회 전용 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/bff/v1/pricing-policies")
@RequiredArgsConstructor
@Tag(name = "Pricing Policy", description = "가격 정책 조회 API")
public class PricingPolicyController {
	
	private final YeYakHaeYoClient yeYakHaeYoClient;
	private final ResponseFactory responseFactory;
	
	/**
	 * 룸별 가격 정책 조회
	 * GET /bff/v1/pricing-policies/{roomId}
	 */
	@GetMapping("/{roomId}")
	@Operation(summary = "가격 정책 조회", description = "특정 룸의 가격 정책을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class)))
	})
	public Mono<ResponseEntity<BaseResponse>> getPricingPolicy(
			@Parameter(description = "룸 ID", required = true) @PathVariable Long roomId,
			ServerHttpRequest req
	) {
		log.info("가격 정책 조회: roomId={}", roomId);
		
		return yeYakHaeYoClient.getPricingPolicy(roomId)
				.map(response -> responseFactory.ok(response, req));
	}
}
