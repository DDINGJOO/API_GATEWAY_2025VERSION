package com.study.api_gateway.controller.comment;

import com.study.api_gateway.client.CommentClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.comment.request.CommentUpdateRequest;
import com.study.api_gateway.dto.comment.request.ReplyCreateRequest;
import com.study.api_gateway.dto.comment.request.RootCommentCreateRequest;
import com.study.api_gateway.util.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "루트 댓글 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "CreateRootCommentSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"commentId\": \"c1\",\n    \"articleId\": \"article-1\",\n    \"writerId\": \"user-1\",\n    \"contents\": \"첫 댓글입니다.\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments\"\n  }\n}")))
    })
    @PostMapping
    public Mono<ResponseEntity<BaseResponse>> createRoot(@RequestBody RootCommentCreateRequest request, ServerHttpRequest req) {
        return commentClient.createRootComment(request)
                .map(result -> responseFactory.ok(result, req));
    }

    // 2) 대댓글 생성
    @Operation(summary = "대댓글 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "CreateReplySuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": {\n    \"commentId\": \"c2\",\n    \"parentCommentId\": \"c1\",\n    \"rootCommentId\": \"c1\",\n    \"depth\": 1,\n    \"contents\": \"대댓글입니다.\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{parentId}/replies\"\n  }\n}")))
    })
    @PostMapping("/{parentId}/replies")
    public Mono<ResponseEntity<BaseResponse>> createReply(@PathVariable String parentId,
                                                          @RequestBody ReplyCreateRequest request,
                                                          ServerHttpRequest req) {
        return commentClient.createReply(parentId, request)
                .map(result -> responseFactory.ok(result, req));
    }

    // 3) 특정 아티클의 전체 댓글 조회
    @Operation(summary = "특정 아티클의 전체 댓글 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "CommentsByArticle", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [ { \"commentId\": \"c1\" } ],\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/article/{articleId}\"\n  }\n}")))
    })
    @GetMapping("/article/{articleId}")
    public Mono<ResponseEntity<BaseResponse>> getByArticle(@PathVariable String articleId, ServerHttpRequest req) {
        return commentClient.getCommentsByArticle(articleId)
                .map(result -> responseFactory.ok(result, req));
    }

    // 4) 특정 부모의 대댓글 목록 조회
    @Operation(summary = "특정 부모의 대댓글 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "RepliesByParent", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [ { \"commentId\": \"c2\", \"parentCommentId\": \"c1\" } ],\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{parentId}/replies\"\n  }\n}")))
    })
    @GetMapping("/{parentId}/replies")
    public Mono<ResponseEntity<BaseResponse>> getReplies(@PathVariable String parentId, ServerHttpRequest req) {
        return commentClient.getReplies(parentId)
                .map(result -> responseFactory.ok(result, req));
    }

    // 5) 루트 댓글 기준 스레드 전체 조회
    @Operation(summary = "루트 댓글 기준 스레드 전체 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "ThreadByRoot", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [ { \"commentId\": \"c1\" }, { \"commentId\": \"c2\" } ],\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/thread/{rootId}\"\n  }\n}")))
    })
    @GetMapping("/thread/{rootId}")
    public Mono<ResponseEntity<BaseResponse>> getThread(@PathVariable String rootId, ServerHttpRequest req) {
        return commentClient.getThread(rootId)
                .map(result -> responseFactory.ok(result, req));
    }

    // 6) 단건 조회
    @Operation(summary = "댓글 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "GetOneComment", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"commentId\": \"c1\", \"contents\": \"내용\" },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}")))
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<BaseResponse>> getOne(@PathVariable String id, ServerHttpRequest req) {
        return commentClient.getOne(id)
                .map(result -> responseFactory.ok(result, req));
    }

    // 7) 댓글 내용 수정
    @Operation(summary = "댓글 내용 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "UpdateCommentSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"commentId\": \"c1\", \"contents\": \"수정\" },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}")))
    })
    @PatchMapping("/{id}")
    public Mono<ResponseEntity<BaseResponse>> update(@PathVariable String id,
                                                     @RequestBody CommentUpdateRequest request,
                                                     ServerHttpRequest req) {
        return commentClient.update(id, request)
                .map(result -> responseFactory.ok(result, req));
    }

    // 8) 소프트 삭제
    @Operation(summary = "댓글 소프트 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "SoftDeleteSuccess", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \"deleted\": true },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}")))
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<BaseResponse>> softDelete(@PathVariable String id,
                                                         @RequestParam String writerId,
                                                         ServerHttpRequest req) {
        return commentClient.softDelete(id, writerId)
                .thenReturn(responseFactory.ok(Map.of("deleted", true), req));
    }
}
