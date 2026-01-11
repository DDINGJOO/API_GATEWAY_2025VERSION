package com.study.api_gateway.api.support.controller;

import com.study.api_gateway.api.support.dto.inquiry.InquiryCategory;
import com.study.api_gateway.api.support.dto.inquiry.InquiryStatus;
import com.study.api_gateway.api.support.dto.inquiry.request.InquiryCreateRequest;
import com.study.api_gateway.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 문의 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Inquiry", description = "문의 관련 API")
public interface InquiryApi {

	@Operation(summary = "문의 생성", description = "새로운 문의를 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성됨")
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> createInquiry(
			@RequestBody InquiryCreateRequest request,
			ServerHttpRequest req);

	@Operation(summary = "문의 상세 조회", description = "문의 ID로 상세 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
	})
	@GetMapping("/{inquiryId}")
	Mono<ResponseEntity<BaseResponse>> getInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req);

	@Operation(summary = "문의 목록 조회", description = "필터 조건에 따라 문의 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping
	Mono<ResponseEntity<BaseResponse>> getInquiries(
			@RequestParam(required = false) String writerId,
			@RequestParam(required = false) InquiryCategory category,
			@RequestParam(required = false) InquiryStatus status,
			ServerHttpRequest req);

	@Operation(summary = "문의 삭제", description = "문의를 삭제합니다. 작성자만 삭제할 수 있습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제됨"),
			@ApiResponse(responseCode = "403", description = "권한 없음"),
			@ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
	})
	@DeleteMapping("/{inquiryId}")
	Mono<ResponseEntity<BaseResponse>> deleteInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req);

	@Operation(summary = "답변 확인", description = "문의에 대한 답변을 확인 처리합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "403", description = "권한 없음"),
			@ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
	})
	@PatchMapping("/{inquiryId}/confirm")
	Mono<ResponseEntity<BaseResponse>> confirmInquiry(
			@PathVariable String inquiryId,
			ServerHttpRequest req);
}
