package com.study.api_gateway.domain.support.controller;

import com.study.api_gateway.common.dto.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.domain.profile.ProfileEnrichmentService;
import com.study.api_gateway.domain.support.client.ReportClient;
import com.study.api_gateway.domain.support.dto.report.ReportCreateRequest;
import com.study.api_gateway.domain.support.enums.ReferenceType;
import com.study.api_gateway.domain.support.enums.ReportSortType;
import com.study.api_gateway.domain.support.enums.ReportStatus;
import com.study.api_gateway.domain.support.enums.SortDirection;
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
@RequestMapping("/bff/v1/support/reports")
@RequiredArgsConstructor
public class ReportController {
	private final ReportClient reportClient;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentService profileEnrichmentUtil;
	
	@Operation(summary = "신고 등록", description = "새로운 신고를 등록합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성됨")
	})
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> createReport(
			@RequestBody ReportCreateRequest request,
			ServerHttpRequest req) {
		return reportClient.createReport(request)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req, HttpStatus.CREATED))
				);
	}
	
	@Operation(summary = "신고 상세 조회", description = "신고 ID로 상세 정보를 조회합니다.(디버깅용으로 쓰세요)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음")
	})
	@GetMapping("/{reportId}")
	public Mono<ResponseEntity<BaseResponse>> getReport(
			@PathVariable String reportId,
			ServerHttpRequest req) {
		return reportClient.getReport(reportId)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
	
	@Operation(summary = "신고 목록 검색 (커서 기반 페이징)",
			description = "필터 조건에 따라 신고 목록을 조회합니다. 커서 기반 페이징을 지원합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공")
	})
	@GetMapping
	public Mono<ResponseEntity<BaseResponse>> getReports(
			@RequestParam(required = false) ReportStatus status,
			@RequestParam(required = false) ReferenceType referenceType,
			@RequestParam(required = false) String reportCategory,
			@RequestParam(required = false) ReportSortType sortType,
			@RequestParam(required = false) SortDirection sortDirection,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false) Integer size,
			ServerHttpRequest req) {
		return reportClient.getReports(status, referenceType, reportCategory, sortType, sortDirection, cursor, size)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
}
