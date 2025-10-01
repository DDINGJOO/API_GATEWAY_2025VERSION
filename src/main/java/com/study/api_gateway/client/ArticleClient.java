package com.study.api_gateway.client;


import com.study.api_gateway.dto.Article.request.ArticleCreateRequest;
import com.study.api_gateway.dto.Article.response.ArticleCursorPageResponse;
import com.study.api_gateway.dto.Article.response.ArticleResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class ArticleClient {
	private final WebClient webClient;
	
	public ArticleClient(@Qualifier(value = "articleWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	
	
	public Mono<ArticleResponse> postArticle(ArticleCreateRequest request) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles")
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	public Mono<ArticleResponse> getArticle(String articleId) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	public Mono<Void> deleteArticle(String articleId) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.delete()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	public Mono<ArticleResponse> updateArticle(String articleId , ArticleCreateRequest req)
	{
		String uriString = UriComponentsBuilder.fromPath("/api/articles/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.put()
				.uri(uriString)
				.bodyValue(req)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	public Mono<ArticleCursorPageResponse> fetchArticleCursorPageResponse(Integer size, String cursorId, Object board, List<?> keyword , String title, String content, List<String> writerIds) {
		String uriString = UriComponentsBuilder.fromPath("/api/articles/search")
				.queryParam("size", size)
				.queryParam("cursorId", cursorId)
				.queryParam("board", board)
				.queryParam("keyword", keyword)
				.queryParam("title", title)
				.queryParam("content", content)
				.queryParam("writerIds", writerIds)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ArticleCursorPageResponse.class);
	}
	
	
	public Mono<Map<Long,String>> getBoards(){
		String uriString = UriComponentsBuilder.fromPath("/api/enums/boards")
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ParameterizedTypeReference.forType(Map.class));
	}
	public Mono<Map<Long,String>> getKeywords(){
		String uriString = UriComponentsBuilder.fromPath("/api/enums/keywords")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ParameterizedTypeReference.forType(Map.class));
	}
}
