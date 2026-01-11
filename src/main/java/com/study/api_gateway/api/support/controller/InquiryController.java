package com.study.api_gateway.api.support.controller;

import com.study.api_gateway.api.support.controller.InquiryApi;
import com.study.api_gateway.api.support.service.SupportFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.api.support.dto.inquiry.InquiryCategory;
import com.study.api_gateway.api.support.dto.inquiry.InquiryStatus;
import com.study.api_gateway.api.support.dto.inquiry.request.InquiryCreateRequest;
import com.study.api_gateway.enrichment.ProfileEnrichmentUtil;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.common.util.UserIdValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/support/inquiries")
@RequiredArgsConstructor
public class InquiryController implements InquiryApi {
	private final SupportFacadeService supportFacadeService;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final UserIdValidator userIdValidator;

	@Override
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> createInquiry(
			@RequestBody InquiryCreateRequest request,
			ServerHttpRequest req) {
		// 토큰에서 userId 추출하여 설정
		String userId = userIdValidator.extractTokenUserId(req);
		request.setWriterId(userId);

		return supportFacadeService.createInquiry(request)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req, HttpStatus.CREATED))
				);
	}

	@Override
	@GetMapping("/{inquiryId}")
	public Mono<ResponseEntity<BaseResponse>> getInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req) {
		return supportFacadeService.getInquiry(inquiryId)
				.flatMap(result -> {
					// 문의 조회 후 작성자 확인 (개인정보 보호)
					String inquiryWriterId = result.getWriterId();

					if (inquiryWriterId == null) {
						return Mono.error(new org.springframework.web.server.ResponseStatusException(
								HttpStatus.INTERNAL_SERVER_ERROR, "문의 작성자 정보를 찾을 수 없습니다"));
					}

					// 토큰의 userId와 문의 작성자 ID가 일치하는지 검증
					return userIdValidator.validateOwnership(req, inquiryWriterId, "문의")
							.then(profileEnrichmentUtil.enrichAny(result))
							.map(enriched -> responseFactory.ok(enriched, req));
				});
	}

	@Override
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getInquiries(
			@RequestParam(required = false) String writerId,
			@RequestParam(required = false) InquiryCategory category,
			@RequestParam(required = false) InquiryStatus status,
			ServerHttpRequest req) {
		// writerId 파라미터는 유지하되, 실제로는 토큰에서 추출한 userId 사용
		String userId = userIdValidator.extractTokenUserId(req);

		return supportFacadeService.getInquiries(userId, category, status)
				.collectList()
				.flatMap(list -> profileEnrichmentUtil.enrichAny(list)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}

	@Override
	@DeleteMapping("/{inquiryId}")
	public Mono<ResponseEntity<BaseResponse>> deleteInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req) {
		// 토큰에서 userId 추출
		String userId = userIdValidator.extractTokenUserId(req);

		return supportFacadeService.deleteInquiry(inquiryId, userId)
				.thenReturn(responseFactory.ok(null, req, HttpStatus.NO_CONTENT));
	}

	@Override
	@PatchMapping("/{inquiryId}/confirm")
	public Mono<ResponseEntity<BaseResponse>> confirmInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req) {
		// 토큰에서 userId 추출
		String userId = userIdValidator.extractTokenUserId(req);

		return supportFacadeService.confirmInquiry(inquiryId, userId)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
}
