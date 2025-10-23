package com.study.api_gateway.domain.image;


import com.study.api_gateway.domain.image.dto.request.ImageBatchConfirmRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

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
		return webClient.patch()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	public Mono<Void> confirmImages(ImageBatchConfirmRequest request) {
		String uriString = UriComponentsBuilder.fromPath("/api/v1/images/confirm")
				.toUriString();
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	public Mono<Map<String, Object>> getExtensions() {
		String uriString = UriComponentsBuilder.fromPath("/api/extensions")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				});
	}
	
	public Mono<Map<String, Object>> getReferenceType() {
		String uriString = UriComponentsBuilder.fromPath("/api/referenceType")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				});
	}
	
}
