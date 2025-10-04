package com.study.api_gateway.client;

import com.study.api_gateway.dto.comment.request.CommentUpdateRequest;
import com.study.api_gateway.dto.comment.request.ReplyCreateRequest;
import com.study.api_gateway.dto.comment.request.RootCommentCreateRequest;
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
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Map<String, Object>> createReply(String parentId, ReplyCreateRequest request) {
        String uri = UriComponentsBuilder.fromPath("/api/comments/{parentId}/replies")
                .buildAndExpand(parentId)
                .toUriString();
        return webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<List<Map<String, Object>>> getCommentsByArticle(String articleId) {
        String uri = UriComponentsBuilder.fromPath("/api/comments/article/{articleId}")
                .buildAndExpand(articleId)
                .toUriString();
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<List<Map<String, Object>>> getReplies(String parentId) {
        String uri = UriComponentsBuilder.fromPath("/api/comments/{parentId}/replies")
                .buildAndExpand(parentId)
                .toUriString();
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<List<Map<String, Object>>> getThread(String rootId) {
        String uri = UriComponentsBuilder.fromPath("/api/comments/thread/{rootId}")
                .buildAndExpand(rootId)
                .toUriString();
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Map<String, Object>> getOne(String id) {
        String uri = UriComponentsBuilder.fromPath("/api/comments/{id}")
                .buildAndExpand(id)
                .toUriString();
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Map<String, Object>> update(String id, CommentUpdateRequest request) {
        String uri = UriComponentsBuilder.fromPath("/api/comments/{id}")
                .buildAndExpand(id)
                .toUriString();
        return webClient.patch()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
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
}
