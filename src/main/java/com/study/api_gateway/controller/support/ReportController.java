package com.study.api_gateway.controller.support;

import com.study.api_gateway.client.ReportClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.support.report.ReferenceType;
import com.study.api_gateway.dto.support.report.ReportSortType;
import com.study.api_gateway.dto.support.report.ReportStatus;
import com.study.api_gateway.dto.support.report.SortDirection;
import com.study.api_gateway.dto.support.report.request.ReportCreateRequest;
import com.study.api_gateway.dto.support.report.request.ReportWithdrawRequest;
import com.study.api_gateway.util.ProfileEnrichmentUtil;
import com.study.api_gateway.util.ResponseFactory;
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
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	
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
	
	@Operation(summary = "신고 철회", description = "신고를 철회합니다. PENDING 상태의 신고만 철회 가능합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "400", description = "철회 불가능한 상태"),
			@ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음")
	})
	@DeleteMapping("/{reportId}")
	public Mono<ResponseEntity<BaseResponse>> withdrawReport(
			@PathVariable String reportId,
			@RequestBody ReportWithdrawRequest request,
			ServerHttpRequest req) {
		return reportClient.withdrawReport(reportId, request)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
}
