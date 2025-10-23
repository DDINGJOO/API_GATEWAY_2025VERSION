package com.study.api_gateway.domain.article.client;

import com.study.api_gateway.domain.article.dto.ArticleCreateRequest;
import com.study.api_gateway.domain.article.dto.EventArticleResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class EventClient {
	private final WebClient webClient;
	
	public EventClient(@Qualifier(value = "articleWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	public Mono<EventArticleResponse> postEvent(ArticleCreateRequest request) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/events")
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(EventArticleResponse.class);
	}
	
	public Mono<EventArticleResponse> getEvent(String articleId) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/events/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(EventArticleResponse.class);
	}
	
	public Mono<Void> deleteEvent(String articleId) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/events/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.delete()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	public Mono<EventArticleResponse> updateEvent(String articleId, ArticleCreateRequest req) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/events/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.put()
				.uri(uriString)
				.bodyValue(req)
				.retrieve()
				.bodyToMono(EventArticleResponse.class);
	}
	
	public Mono<Object> getEvents(String status, Integer page, Integer size) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/events")
				.queryParam("status", status)
				.queryParam("page", page)
				.queryParam("size", size)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Object.class);
	}
}
