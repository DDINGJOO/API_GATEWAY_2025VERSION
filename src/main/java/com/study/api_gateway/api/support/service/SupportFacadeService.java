package com.study.api_gateway.api.support.service;

import com.study.api_gateway.api.support.client.FaqClient;
import com.study.api_gateway.api.support.client.InquiryClient;
import com.study.api_gateway.api.support.client.ReportClient;
import com.study.api_gateway.api.support.dto.faq.FaqCategory;
import com.study.api_gateway.api.support.dto.faq.response.FaqResponse;
import com.study.api_gateway.api.support.dto.inquiry.InquiryCategory;
import com.study.api_gateway.api.support.dto.inquiry.InquiryStatus;
import com.study.api_gateway.api.support.dto.inquiry.request.InquiryCreateRequest;
import com.study.api_gateway.api.support.dto.inquiry.response.InquiryResponse;
import com.study.api_gateway.api.support.dto.report.ReferenceType;
import com.study.api_gateway.api.support.dto.report.ReportSortType;
import com.study.api_gateway.api.support.dto.report.ReportStatus;
import com.study.api_gateway.api.support.dto.report.SortDirection;
import com.study.api_gateway.api.support.dto.report.request.ReportCreateRequest;
import com.study.api_gateway.api.support.dto.report.request.ReportWithdrawRequest;
import com.study.api_gateway.api.support.dto.report.response.ReportPageResponse;
import com.study.api_gateway.api.support.dto.report.response.ReportResponse;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Support 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class SupportFacadeService {

	private final FaqClient faqClient;
	private final InquiryClient inquiryClient;
	private final ReportClient reportClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "support-service";

	// ==================== FAQ API ====================

	public Flux<FaqResponse> getFaqs(FaqCategory category) {
		return faqClient.getFaqs(category)
				.transform(resilience.protectFlux(SERVICE_NAME));
	}

	// ==================== Inquiry API ====================

	public Mono<InquiryResponse> createInquiry(InquiryCreateRequest request) {
		return inquiryClient.createInquiry(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<InquiryResponse> getInquiry(String inquiryId) {
		return inquiryClient.getInquiry(inquiryId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Flux<InquiryResponse> getInquiries(String writerId, InquiryCategory category, InquiryStatus status) {
		return inquiryClient.getInquiries(writerId, category, status)
				.transform(resilience.protectFlux(SERVICE_NAME));
	}

	public Mono<Void> deleteInquiry(String inquiryId, String writerId) {
		return inquiryClient.deleteInquiry(inquiryId, writerId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<InquiryResponse> confirmInquiry(String inquiryId, String writerId) {
		return inquiryClient.confirmInquiry(inquiryId, writerId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	// ==================== Report API ====================

	public Mono<ReportResponse> createReport(ReportCreateRequest request) {
		return reportClient.createReport(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ReportResponse> getReport(String reportId) {
		return reportClient.getReport(reportId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ReportPageResponse> getReports(
			ReportStatus status,
			ReferenceType referenceType,
			String reportCategory,
			ReportSortType sortType,
			SortDirection sortDirection,
			String cursor,
			Integer size
	) {
		return reportClient.getReports(status, referenceType, reportCategory, sortType, sortDirection, cursor, size)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ReportResponse> withdrawReport(String reportId, ReportWithdrawRequest request) {
		return reportClient.withdrawReport(reportId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
