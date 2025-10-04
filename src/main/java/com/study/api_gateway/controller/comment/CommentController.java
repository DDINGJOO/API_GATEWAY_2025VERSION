package com.study.api_gateway.controller.comment;

import com.study.api_gateway.client.CommentClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.comment.request.CommentUpdateRequest;
import com.study.api_gateway.dto.comment.request.ReplyCreateRequest;
import com.study.api_gateway.dto.comment.request.RootCommentCreateRequest;
import com.study.api_gateway.util.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/v1/communities/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentClient commentClient;
    private final ResponseFactory responseFactory;

    // 1) 루트 댓글 생성
    @PostMapping
    public Mono<ResponseEntity<BaseResponse>> createRoot(@RequestBody RootCommentCreateRequest request, ServerHttpRequest req) {
        return commentClient.createRootComment(request)
                .map(result -> responseFactory.ok(result, req));
    }

    // 2) 대댓글 생성
    @PostMapping("/{parentId}/replies")
    public Mono<ResponseEntity<BaseResponse>> createReply(@PathVariable String parentId,
                                                          @RequestBody ReplyCreateRequest request,
                                                          ServerHttpRequest req) {
        return commentClient.createReply(parentId, request)
                .map(result -> responseFactory.ok(result, req));
    }

    // 3) 특정 아티클의 전체 댓글 조회
    @GetMapping("/article/{articleId}")
    public Mono<ResponseEntity<BaseResponse>> getByArticle(@PathVariable String articleId, ServerHttpRequest req) {
        return commentClient.getCommentsByArticle(articleId)
                .map(result -> responseFactory.ok(result, req));
    }

    // 4) 특정 부모의 대댓글 목록 조회
    @GetMapping("/{parentId}/replies")
    public Mono<ResponseEntity<BaseResponse>> getReplies(@PathVariable String parentId, ServerHttpRequest req) {
        return commentClient.getReplies(parentId)
                .map(result -> responseFactory.ok(result, req));
    }

    // 5) 루트 댓글 기준 스레드 전체 조회
    @GetMapping("/thread/{rootId}")
    public Mono<ResponseEntity<BaseResponse>> getThread(@PathVariable String rootId, ServerHttpRequest req) {
        return commentClient.getThread(rootId)
                .map(result -> responseFactory.ok(result, req));
    }

    // 6) 단건 조회
    @GetMapping("/{id}")
    public Mono<ResponseEntity<BaseResponse>> getOne(@PathVariable String id, ServerHttpRequest req) {
        return commentClient.getOne(id)
                .map(result -> responseFactory.ok(result, req));
    }

    // 7) 댓글 내용 수정
    @PatchMapping("/{id}")
    public Mono<ResponseEntity<BaseResponse>> update(@PathVariable String id,
                                                     @RequestBody CommentUpdateRequest request,
                                                     ServerHttpRequest req) {
        return commentClient.update(id, request)
                .map(result -> responseFactory.ok(result, req));
    }

    // 8) 소프트 삭제
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<BaseResponse>> softDelete(@PathVariable String id,
                                                         @RequestParam String writerId,
                                                         ServerHttpRequest req) {
        return commentClient.softDelete(id, writerId)
                .thenReturn(responseFactory.ok(Map.of("deleted", true), req));
    }
}
