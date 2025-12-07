package com.study.api_gateway.client;

import com.study.api_gateway.dto.comment.request.CommentUpdateRequest;
import com.study.api_gateway.dto.comment.request.ReplyCreateRequest;
import com.study.api_gateway.dto.comment.request.RootCommentCreateRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments")
                        .build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Map<String, Object>> createReply(String parentId, ReplyCreateRequest request) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/{parentId}/replies")
                        .build(parentId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<List<Map<String, Object>>> getCommentsByArticle(String articleId) {
	    // backward-compatible default: no pagination params
	    return getCommentsByArticle(articleId, null, null, null);
    }

	public Mono<List<Map<String, Object>>> getCommentsByArticle(String articleId, Integer page, Integer pageSize, String mode) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/comments/article/{articleId}");

                    if (page != null) uriBuilder.queryParam("page", page);
                    if (pageSize != null) uriBuilder.queryParam("pageSize", pageSize);
                    if (mode != null && !mode.isBlank()) uriBuilder.queryParam("mode", mode);

                    return uriBuilder.build(articleId);
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<List<Map<String, Object>>> getReplies(String parentId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/{parentId}/replies")
                        .build(parentId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<List<Map<String, Object>>> getThread(String rootId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/thread/{rootId}")
                        .build(rootId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Map<String, Object>> getOne(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/{id}")
                        .build(id))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Map<String, Object>> update(String id, CommentUpdateRequest request) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/{id}")
                        .build(id))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Void> softDelete(String id, String writerId) {
        return webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/comments/{id}")
                        .queryParam("writerId", writerId)
                        .build(id))
                .retrieve()
                .bodyToMono(Void.class);
    }

	// New: counts for multiple articles
	public Mono<Map<String, Integer>> getCountsForArticles(List<String> articleIds) {
		return webClient.post()
				.uri(uriBuilder -> uriBuilder
                        .path("/api/comments/articles/counts")
                        .build())
				.bodyValue(articleIds)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<>() {
				});
	}
}