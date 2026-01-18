package com.study.api_gateway.api.comment.service;

import com.study.api_gateway.api.comment.client.CommentClient;
import com.study.api_gateway.api.comment.dto.request.CommentUpdateRequest;
import com.study.api_gateway.api.comment.dto.request.ReplyCreateRequest;
import com.study.api_gateway.api.comment.dto.request.RootCommentCreateRequest;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Comment 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class CommentFacadeService {
	
	private static final String SERVICE_NAME = "comment-service";
	private final CommentClient commentClient;
	private final ResilienceOperator resilience;
	
	public Mono<Map<String, Object>> createRootComment(RootCommentCreateRequest request) {
		return commentClient.createRootComment(request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, Object>> createReply(String parentId, ReplyCreateRequest request) {
		return commentClient.createReply(parentId, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<Map<String, Object>>> getCommentsByArticle(String articleId) {
		return commentClient.getCommentsByArticle(articleId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<Map<String, Object>>> getCommentsByArticle(String articleId, Integer page, Integer pageSize, String mode) {
		return commentClient.getCommentsByArticle(articleId, page, pageSize, mode)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<Map<String, Object>>> getReplies(String parentId) {
		return commentClient.getReplies(parentId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<List<Map<String, Object>>> getThread(String rootId) {
		return commentClient.getThread(rootId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, Object>> getOne(String id) {
		return commentClient.getOne(id)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, Object>> update(String id, CommentUpdateRequest request) {
		return commentClient.update(id, request)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Void> softDelete(String id, String writerId) {
		return commentClient.softDelete(id, writerId)
				.transform(resilience.protect(SERVICE_NAME));
	}
	
	public Mono<Map<String, Integer>> getCountsForArticles(List<String> articleIds) {
		return commentClient.getCountsForArticles(articleIds)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
