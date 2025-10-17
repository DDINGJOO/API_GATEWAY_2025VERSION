package com.study.api_gateway.client;

import com.study.api_gateway.dto.support.inquiry.InquiryCategory;
import com.study.api_gateway.dto.support.inquiry.InquiryStatus;
import com.study.api_gateway.dto.support.inquiry.request.InquiryCreateRequest;
import com.study.api_gateway.dto.support.inquiry.response.InquiryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class InquiryClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/v1/inquiries";
	
	public InquiryClient(@Qualifier(value = "supportWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * 문의 생성
	 * POST /api/v1/inquiries
	 */
	public Mono<InquiryResponse> createInquiry(InquiryCreateRequest request) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX)
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(InquiryResponse.class);
	}
	
	/**
	 * 문의 상세 조회
	 * GET /api/v1/inquiries/{inquiryId}
	 */
	public Mono<InquiryResponse> getInquiry(String inquiryId) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/{inquiryId}")
				.buildAndExpand(inquiryId)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(InquiryResponse.class);
	}
	
	/**
	 * 문의 목록 조회
	 * GET /api/v1/inquiries
	 */
	public Flux<InquiryResponse> getInquiries(String writerId, InquiryCategory category, InquiryStatus status) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX);
		
		if (writerId != null && !writerId.isBlank()) {
			builder.queryParam("writerId", writerId);
		}
		if (category != null) {
			builder.queryParam("category", category);
		}
		if (status != null) {
			builder.queryParam("status", status);
		}
		
		String uriString = builder.toUriString();
		
		log.info("getInquiries uriString: {}", uriString);
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<InquiryResponse>>() {
				})
				.flatMapMany(list -> Flux.fromIterable(list == null ? List.of() : list));
	}
	
	/**
	 * 문의 삭제
	 * DELETE /api/v1/inquiries/{inquiryId}
	 */
	public Mono<Void> deleteInquiry(String inquiryId, String writerId) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/{inquiryId}")
				.queryParam("writerId", writerId)
				.buildAndExpand(inquiryId)
				.toUriString();
		
		return webClient.delete()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	/**
	 * 답변 확인
	 * PATCH /api/v1/inquiries/{inquiryId}/confirm
	 */
	public Mono<InquiryResponse> confirmInquiry(String inquiryId, String writerId) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/{inquiryId}/confirm")
				.queryParam("writerId", writerId)
				.buildAndExpand(inquiryId)
				.toUriString();
		
		return webClient.patch()
				.uri(uriString)
				.retrieve()
				.bodyToMono(InquiryResponse.class);
	}
}
