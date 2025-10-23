package com.study.api_gateway.domain.comment;

import com.study.api_gateway.domain.comment.dto.CommentUpdateRequest;
import com.study.api_gateway.domain.comment.dto.ReplyCreateRequest;
import com.study.api_gateway.domain.comment.dto.RootCommentCreateRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class CommentClient {
	
	private final WebClient webClient;
	
	public CommentClient(@Qualifier(value = "commentWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	public Mono<Map<String, Object>> createRootComment(RootCommentCreateRequest request) {
		String uri = UriComponentsBuilder.fromPath("/api/comments").toUriString();
		return webClient.post()
				.uri(uri)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	public Mono<Map<String, Object>> createReply(String parentId, ReplyCreateRequest request) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/{parentId}/replies")
				.buildAndExpand(parentId)
				.toUriString();
		return webClient.post()
				.uri(uri)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	public Mono<List<Map<String, Object>>> getCommentsByArticle(String articleId) {
		// backward-compatible default: no pagination params
		return getCommentsByArticle(articleId, null, null, null);
	}
	
	public Mono<List<Map<String, Object>>> getCommentsByArticle(String articleId, Integer page, Integer pageSize, String mode) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/article/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromUriString(uri);
		if (page != null) queryBuilder.queryParam("page", page);
		if (pageSize != null) queryBuilder.queryParam("pageSize", pageSize);
		if (mode != null && !mode.isBlank()) queryBuilder.queryParam("mode", mode);
		String finalUri = queryBuilder.toUriString();
		return webClient.get()
				.uri(finalUri)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	public Mono<List<Map<String, Object>>> getReplies(String parentId) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/{parentId}/replies")
				.buildAndExpand(parentId)
				.toUriString();
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	public Mono<List<Map<String, Object>>> getThread(String rootId) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/thread/{rootId}")
				.buildAndExpand(rootId)
				.toUriString();
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	public Mono<Map<String, Object>> getOne(String id) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/{id}")
				.buildAndExpand(id)
				.toUriString();
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	public Mono<Map<String, Object>> update(String id, CommentUpdateRequest request) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/{id}")
				.buildAndExpand(id)
				.toUriString();
		return webClient.patch()
				.uri(uri)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
	
	public Mono<Void> softDelete(String id, String writerId) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/{id}")
				.queryParam("writerId", writerId)
				.buildAndExpand(id)
				.toUriString();
		return webClient.delete()
				.uri(uri)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	// New: counts for multiple articles
	public Mono<Map<String, Integer>> getCountsForArticles(List<String> articleIds) {
		String uri = UriComponentsBuilder.fromPath("/api/comments/articles/counts").toUriString();
		return webClient.post()
				.uri(uri)
				.bodyValue(articleIds)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
}
