package com.study.api_gateway.api.support.controller;

import com.study.api_gateway.api.support.dto.report.ReferenceType;
import com.study.api_gateway.api.support.dto.report.ReportSortType;
import com.study.api_gateway.api.support.dto.report.ReportStatus;
import com.study.api_gateway.api.support.dto.report.SortDirection;
import com.study.api_gateway.api.support.dto.report.request.ReportCreateRequest;
import com.study.api_gateway.api.support.dto.report.request.ReportWithdrawRequest;
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
 * 신고 API 인터페이스
 * Swagger 문서와 API 명세를 정의
 */
@Tag(name = "Report", description = "신고 관련 API")
public interface ReportApi {

	@Operation(summary = "신고 등록", description = "새로운 신고를 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성됨")
	})
	@PostMapping
	Mono<ResponseEntity<BaseResponse>> createReport(
			@RequestBody ReportCreateRequest request,
			ServerHttpRequest req);

	@Operation(summary = "신고 상세 조회", description = "신고 ID로 상세 정보를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음")
	})
	@GetMapping("/{reportId}")
	Mono<ResponseEntity<BaseResponse>> getReport(
			@PathVariable String reportId,
			ServerHttpRequest req);

	@Operation(summary = "신고 목록 검색 (커서 기반 페이징)",
			description = "필터 조건에 따라 신고 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping
	Mono<ResponseEntity<BaseResponse>> getReports(
			@RequestParam(required = false) ReportStatus status,
			@RequestParam(required = false) ReferenceType referenceType,
			@RequestParam(required = false) String reportCategory,
			@RequestParam(required = false) ReportSortType sortType,
			@RequestParam(required = false) SortDirection sortDirection,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false) Integer size,
			ServerHttpRequest req);

	@Operation(summary = "신고 철회", description = "신고를 철회합니다. PENDING 상태의 신고만 철회 가능합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "400", description = "철회 불가능한 상태"),
			@ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음")
	})
	@DeleteMapping("/{reportId}")
	Mono<ResponseEntity<BaseResponse>> withdrawReport(
			@PathVariable String reportId,
			@RequestBody ReportWithdrawRequest request,
			ServerHttpRequest req);
}
