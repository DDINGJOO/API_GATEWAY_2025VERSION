package com.study.api_gateway.domain.article.client;

import com.study.api_gateway.domain.article.dto.ArticleCreateRequest;
import com.study.api_gateway.domain.article.dto.ArticleResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
public class NoticeClient {
	private final WebClient webClient;
	
	public NoticeClient(@Qualifier(value = "articleWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	public Mono<ArticleResponse> postNotice(ArticleCreateRequest request) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/notices")
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	public Mono<ArticleResponse> getNotice(String articleId) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/notices/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	public Mono<Void> deleteNotice(String articleId) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/notices/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.delete()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	public Mono<ArticleResponse> updateNotice(String articleId, ArticleCreateRequest req) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/notices/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.put()
				.uri(uriString)
				.bodyValue(req)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	public Mono<Object> getNotices(Integer page, Integer size) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/notices")
				.queryParam("page", page)
				.queryParam("size", size)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Object.class);
	}
}
