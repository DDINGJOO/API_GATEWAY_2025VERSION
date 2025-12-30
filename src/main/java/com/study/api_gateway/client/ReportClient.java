package com.study.api_gateway.client;

import com.study.api_gateway.dto.support.report.ReferenceType;
import com.study.api_gateway.dto.support.report.ReportSortType;
import com.study.api_gateway.dto.support.report.ReportStatus;
import com.study.api_gateway.dto.support.report.SortDirection;
import com.study.api_gateway.dto.support.report.request.ReportCreateRequest;
import com.study.api_gateway.dto.support.report.request.ReportWithdrawRequest;
import com.study.api_gateway.dto.support.report.response.ReportPageResponse;
import com.study.api_gateway.dto.support.report.response.ReportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReportClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/v1/reports";
	
	public ReportClient(@Qualifier(value = "supportWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 신고 등록
	 * POST /api/v1/reports
	 */
	public Mono<ReportResponse> createReport(ReportCreateRequest request) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX)
				.toUriString();

		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ReportResponse.class);
	}
	
	/**
	 * 신고 상세 조회
	 * GET /api/v1/reports/{reportId}
	 */
	public Mono<ReportResponse> getReport(String reportId) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/{reportId}")
				.buildAndExpand(reportId)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ReportResponse.class);
	}
	
	/**
	 * 신고 목록 검색 (커서 기반 페이징)
	 * GET /api/v1/reports
	 */
	public Mono<ReportPageResponse> getReports(
			ReportStatus status,
			ReferenceType referenceType,
			String reportCategory,
			ReportSortType sortType,
			SortDirection sortDirection,
			String cursor,
			Integer size
	) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX);
		
		if (status != null) {
			builder.queryParam("status", status);
		}
		if (referenceType != null) {
			builder.queryParam("referenceType", referenceType);
		}
		if (reportCategory != null && !reportCategory.isBlank()) {
			builder.queryParam("reportCategory", reportCategory);
		}
		if (sortType != null) {
			builder.queryParam("sortType", sortType);
		}
		if (sortDirection != null) {
			builder.queryParam("sortDirection", sortDirection);
		}
		if (cursor != null && !cursor.isBlank()) {
			builder.queryParam("cursor", cursor);
		}
		if (size != null) {
			builder.queryParam("size", size);
		}
		
		String uriString = builder.toUriString();
		
		log.info("getReports uriString: {}", uriString);
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ReportPageResponse.class);
	}
	
	/**
	 * 신고 철회
	 * DELETE /api/v1/reports/{reportId}
	 */
	public Mono<ReportResponse> withdrawReport(String reportId, ReportWithdrawRequest request) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/{reportId}")
				.buildAndExpand(reportId)
				.toUriString();
		
		return webClient.method(org.springframework.http.HttpMethod.DELETE)
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ReportResponse.class);
	}
}
