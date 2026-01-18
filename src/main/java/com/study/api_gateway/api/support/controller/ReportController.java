package com.study.api_gateway.api.support.controller;

import com.study.api_gateway.api.support.dto.report.ReferenceType;
import com.study.api_gateway.api.support.dto.report.ReportSortType;
import com.study.api_gateway.api.support.dto.report.ReportStatus;
import com.study.api_gateway.api.support.dto.report.SortDirection;
import com.study.api_gateway.api.support.dto.report.request.ReportCreateRequest;
import com.study.api_gateway.api.support.dto.report.request.ReportWithdrawRequest;
import com.study.api_gateway.api.support.service.SupportFacadeService;
import com.study.api_gateway.common.response.BaseResponse;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.enrichment.ProfileEnrichmentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/support/reports")
@RequiredArgsConstructor
public class ReportController implements ReportApi {
	private final SupportFacadeService supportFacadeService;
	private final ResponseFactory responseFactory;
	private final ProfileEnrichmentUtil profileEnrichmentUtil;
	
	@Override
	@PostMapping
	public Mono<ResponseEntity<BaseResponse>> createReport(
			@RequestBody ReportCreateRequest request,
			ServerHttpRequest req) {
		return supportFacadeService.createReport(request)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req, HttpStatus.CREATED))
				);
	}
	
	@Override
	@GetMapping("/{reportId}")
	public Mono<ResponseEntity<BaseResponse>> getReport(
			@PathVariable String reportId,
			ServerHttpRequest req) {
		return supportFacadeService.getReport(reportId)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
	
	@Override
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
		return supportFacadeService.getReports(status, referenceType, reportCategory, sortType, sortDirection, cursor, size)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
	
	@Override
	@DeleteMapping("/{reportId}")
	public Mono<ResponseEntity<BaseResponse>> withdrawReport(
			@PathVariable String reportId,
			@RequestBody ReportWithdrawRequest request,
			ServerHttpRequest req) {
		return supportFacadeService.withdrawReport(reportId, request)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}
}
