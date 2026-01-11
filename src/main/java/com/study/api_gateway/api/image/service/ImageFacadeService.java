package com.study.api_gateway.api.image.service;

import com.study.api_gateway.api.image.client.ImageClient;
import com.study.api_gateway.api.image.dto.response.ExtensionDto;
import com.study.api_gateway.api.image.dto.response.ReferenceTypeDto;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Image 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class ImageFacadeService {

	private final ImageClient imageClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "image-service";

	public Mono<Void> confirmImage(String referenceId, String imageId) {
		return imageClient.confirmImage(referenceId, imageId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Void> confirmImage(String referenceId, List<String> imageIds) {
		return imageClient.confirmImage(referenceId, imageIds)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Map<String, ExtensionDto>> getExtensions() {
		return imageClient.getExtensions()
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Map<String, ReferenceTypeDto>> getReferenceType() {
		return imageClient.getReferenceType()
				.transform(resilience.protect(SERVICE_NAME));
	}
}
