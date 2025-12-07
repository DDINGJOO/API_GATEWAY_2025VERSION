package com.study.api_gateway.client;


import com.study.api_gateway.dto.image.request.ImageConfirmRequest;
import com.study.api_gateway.dto.image.response.ExtensionDto;
import com.study.api_gateway.dto.image.response.ReferenceTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ImageClient {
    private final WebClient webClient;

    public ImageClient(@Qualifier(value = "imageWebClient") WebClient webClient) {
        this.webClient = webClient;
    }
	
	
	public Mono<Void> confirmImage(String referenceId, String imageId) {
		
		String uriString = UriComponentsBuilder.fromPath("/api/v1/images/confirm/" + referenceId)
				.queryParam("imageId", imageId)
				.toUriString();
		
		log.info("confirmImage uriString : {}", uriString);
		return webClient.post()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}

	
	/**
	 * 이미지 확정 처리 (배치)
	 * POST /api/v1/images/confirm
	 *
	 * @param referenceId 참조 ID (사용자 ID 등)
	 * @param imageIds    확정할 이미지 ID 목록
	 * @return Mono<Void>
	 */
	public Mono<Void> confirmImage(String referenceId, List<String> imageIds) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/images/confirm")
				.toUriString();

		ImageConfirmRequest request = ImageConfirmRequest.builder()
				.referenceId(referenceId)
				.imageIds(imageIds)
				.build();

		log.info("Confirming batch images - referenceId: {}, imageIds: {}, uri: {}, request body: {}",
			referenceId, imageIds, uriString, request);

		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(Void.class)
				.doOnSuccess(v -> log.info("Batch image confirmation success - referenceId: {}, imageIds: {}",
					referenceId, imageIds))
				.doOnError(error -> log.error("Batch image confirmation failed - referenceId: {}, imageIds: {}, error: {}",
					referenceId, imageIds, error.getMessage(), error));
	}
	
	/**
	 * 이미지 확장자 Enum 조회
	 * GET /api/extensions
	 *
	 * @return key: 확장자 코드, value: 확장자 정보 (code, name)
	 */
	public Mono<Map<String, ExtensionDto>> getExtensions() {
		String uriString = UriComponentsBuilder.fromPath("/api/extensions")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, ExtensionDto>>() {
				});
	}
	
	/**
	 * 이미지 참조 타입 Enum 조회
	 * GET /api/referenceType
	 *
	 * @return key: 참조 타입 코드, value: 참조 타입 정보 (code, name, allowsMultiple, maxImages, description)
	 */
	public Mono<Map<String, ReferenceTypeDto>> getReferenceType() {
		String uriString = UriComponentsBuilder.fromPath("/api/referenceType")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, ReferenceTypeDto>>() {});
	}

}
