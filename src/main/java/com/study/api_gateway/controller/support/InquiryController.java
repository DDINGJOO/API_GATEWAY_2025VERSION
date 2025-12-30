package com.study.api_gateway.controller.support;

import com.study.api_gateway.client.InquiryClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.support.inquiry.InquiryCategory;
import com.study.api_gateway.dto.support.inquiry.InquiryStatus;
import com.study.api_gateway.dto.support.inquiry.request.InquiryCreateRequest;
import com.study.api_gateway.util.ProfileEnrichmentUtil;
import com.study.api_gateway.util.ResponseFactory;
import com.study.api_gateway.util.UserIdValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/support/inquiries")
@RequiredArgsConstructor
public class InquiryController {
	private final InquiryClient inquiryClient;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	private final UserIdValidator userIdValidator;
	
	@Operation(summary = "문의 생성", description = "새로운 문의를 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성됨")
	})
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> createInquiry(
			@RequestBody InquiryCreateRequest request,
			ServerHttpRequest req) {
		// 토큰에서 userId 추출하여 설정
		String userId = userIdValidator.extractTokenUserId(req);
		request.setWriterId(userId);
		
		return inquiryClient.createInquiry(request)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req, HttpStatus.CREATED))
				);
	}
	
	@Operation(summary = "문의 상세 조회", description = "문의 ID로 상세 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
	})
	@GetMapping("/{inquiryId}")
	public Mono<ResponseEntity<BaseResponse>> getInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req) {
		return inquiryClient.getInquiry(inquiryId)
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
	
	@Operation(summary = "문의 목록 조회", description = "필터 조건에 따라 문의 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getInquiries(
			@RequestParam(required = false) String writerId,
			@RequestParam(required = false) InquiryCategory category,
			@RequestParam(required = false) InquiryStatus status,
			ServerHttpRequest req) {
		// writerId 파라미터는 유지하되, 실제로는 토큰에서 추출한 userId 사용
		String userId = userIdValidator.extractTokenUserId(req);
		
		return inquiryClient.getInquiries(userId, category, status)
				.collectList()
				.flatMap(list -> profileEnrichmentUtil.enrichAny(list)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
	
	@Operation(summary = "문의 삭제", description = "문의를 삭제합니다. 작성자만 삭제할 수 있습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제됨"),
			@ApiResponse(responseCode = "403", description = "권한 없음"),
			@ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
	})
	@DeleteMapping("/{inquiryId}")
	public Mono<ResponseEntity<BaseResponse>> deleteInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req) {
		// 토큰에서 userId 추출
		String userId = userIdValidator.extractTokenUserId(req);
		
		return inquiryClient.deleteInquiry(inquiryId, userId)
				.thenReturn(responseFactory.ok(null, req, HttpStatus.NO_CONTENT));
	}
	
	@Operation(summary = "답변 확인", description = "문의에 대한 답변을 확인 처리합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "403", description = "권한 없음"),
			@ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
	})
	@PatchMapping("/{inquiryId}/confirm")
	public Mono<ResponseEntity<BaseResponse>> confirmInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req) {
		// 토큰에서 userId 추출
		String userId = userIdValidator.extractTokenUserId(req);
		
		return inquiryClient.confirmInquiry(inquiryId, userId)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
}
