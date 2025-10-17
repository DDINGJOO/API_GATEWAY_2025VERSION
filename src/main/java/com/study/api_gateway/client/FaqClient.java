package com.study.api_gateway.client;

import com.study.api_gateway.dto.support.faq.FaqCategory;
import com.study.api_gateway.dto.support.faq.response.FaqResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class FaqClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/v1/faqs";
	
	public FaqClient(@Qualifier(value = "supportWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	/**
	 * FAQ 목록 조회
	 * GET /api/v1/faqs
	 */
	public Flux<FaqResponse> getFaqs(FaqCategory category) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX);
		
		if (category != null) {
			builder.queryParam("category", category);
		} else {
			builder.queryParam("category", FaqCategory.ALL);
		}
		
		String uriString = builder.toUriString();
		
		log.info("getFaqs uriString: {}", uriString);
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<FaqResponse>>() {
				})
				.flatMapMany(list -> Flux.fromIterable(list == null ? List.of() : list));
	}
}
