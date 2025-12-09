package com.study.api_gateway.controller.comment;

import com.study.api_gateway.client.CommentClient;
import com.study.api_gateway.dto.BaseResponse;
import com.study.api_gateway.dto.comment.request.CombinedCommentCreateRequest;
import com.study.api_gateway.dto.comment.request.CommentUpdateRequest;
import com.study.api_gateway.dto.comment.request.ReplyCreateRequest;
import com.study.api_gateway.dto.comment.request.RootCommentCreateRequest;
import com.study.api_gateway.util.ResponseFactory;
import com.study.api_gateway.util.UserIdValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff/v1/communities/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {
	private final CommentClient commentClient;
	private final ResponseFactory responseFactory;
	private final com.study.api_gateway.util.ProfileEnrichmentUtil profileEnrichmentUtil;
	private final UserIdValidator userIdValidator;

//    // 1) 루트 댓글 생성
//    @Operation(summary = "루트 댓글 생성",
//            description = "지정한 articleId에 루트 댓글을 생성합니다. 성공 시 생성된 댓글 객체를 반환하며, BFF 응답 status는 201(Created)입니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "201", description = "생성됨",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "CreateRootComment201", value = "{\n  \"isSuccess\": true,\n  \"code\": 201,\n  \"data\": {\n    \"commentId\": \"generated-id\",\n    \"articleId\": \"article-1\",\n    \"writerId\": \"user-1\",\n    \"parentCommentId\": null,\n    \"rootCommentId\": \"generated-id\",\n    \"depth\": 0,\n    \"contents\": \"첫 댓글입니다.\",\n    \"isDeleted\": false,\n    \"status\": \"ACTIVE\",\n    \"replyCount\": 0,\n    \"createdAt\": \"2025-10-03T10:23:45Z\",\n    \"updatedAt\": \"2025-10-03T10:23:45Z\",\n    \"deletedAt\": null\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments\",\n    \"url\": \"http://localhost:8080/bff/v1/communities/comments\"\n  }\n}"))),
//            @ApiResponse(responseCode = "400", description = "유효성 오류",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "CreateRootComment400", value = "{\n  \"isSuccess\": false,\n  \"code\": 400,\n  \"data\": \"writerId는 필수입니다.\",\n  \"request\": { \n    \"path\": \"/bff/v1/communities/comments\"\n  }\n}")))
//    })
//    @PostMapping
//    public Mono<ResponseEntity<BaseResponse>> createRoot(@RequestBody RootCommentCreateRequest request, ServerHttpRequest req) {
//        return commentClient.createRootComment(request)
//                .map(result -> responseFactory.ok(result, req, org.springframework.http.HttpStatus.CREATED));
//    }
//
//    // 2) 대댓글 생성
//    @Operation(summary = "대댓글 생성",
//            description = "parentId로 지정된 부모 댓글 아래에 대댓글을 생성합니다. 성공 시 201(Created)와 함께 생성된 댓글 객체를 반환합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "201", description = "생성됨",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "CreateReply201", value = "{\n  \"isSuccess\": true,\n  \"code\": 201,\n  \"data\": {\n    \"commentId\": \"generated-reply-id\",\n    \"articleId\": \"article-1\",\n    \"writerId\": \"user-2\",\n    \"parentCommentId\": \"parent-id\",\n    \"rootCommentId\": \"root-id\",\n    \"depth\": 1,\n    \"contents\": \"부모 댓글에 대한 대댓글입니다.\",\n    \"isDeleted\": false,\n    \"status\": \"ACTIVE\",\n    \"replyCount\": 0,\n    \"createdAt\": \"2025-10-03T10:24:00Z\",\n    \"updatedAt\": \"2025-10-03T10:24:00Z\",\n    \"deletedAt\": null\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{parentId}/replies\"\n  }\n}"))),
//            @ApiResponse(responseCode = "404", description = "부모 없음",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "ReplyParentNotFound404", value = "{\n  \"isSuccess\": false,\n  \"code\": 404,\n  \"data\": \"부모 댓글을 찾을 수 없습니다.\",\n  \"request\": { \n    \"path\": \"/bff/v1/communities/comments/{parentId}/replies\"\n  }\n}")))
//    })
//    @PostMapping("/{parentId}/replies")
//    public Mono<ResponseEntity<BaseResponse>> createReply(@PathVariable String parentId,
//                                                          @RequestBody ReplyCreateRequest request,
//                                                          ServerHttpRequest req) {
//        return commentClient.createReply(parentId, request)
//                .map(result -> responseFactory.ok(result, req, org.springframework.http.HttpStatus.CREATED));
//    }
	
	// 2-1) 루트/대댓글 통합 생성 (parentId 파라미터로 분기)
	@Operation(summary = "루트/대댓글 통합 생성",
			description = "parentId 파라미터가 없으면 루트 댓글을, 있으면 해당 부모에 대한 대댓글을 생성합니다. 성공 시 201을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "생성됨",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "CreateCombined201", value = "{\n  \"isSuccess\": true,\n  \"code\": 201,\n  \"data\": { \n    \"commentId\": \"generated-id\",\n    \"contents\": \"내용\"\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/create?parentId=optional\"\n  }\n}")))
	})
	@PostMapping("/create")
	public Mono<ResponseEntity<BaseResponse>> createCombined(@RequestParam(required = false) String parentId,
	                                                         @RequestBody CombinedCommentCreateRequest request,
	                                                         ServerHttpRequest req) {
		// 토큰에서 userId 추출하여 설정
		String userId = userIdValidator.extractTokenUserId(req);
		request.setWriterId(userId);
		
		if (parentId == null || parentId.isBlank()) {
			RootCommentCreateRequest root = new RootCommentCreateRequest();
			root.setArticleId(request.getArticleId());
			root.setWriterId(userId);
			root.setContents(request.getContents());
			if (root.getArticleId() == null || root.getArticleId().isBlank()) {
				return Mono.just(responseFactory.ok("articleId는 필수입니다.", req, HttpStatus.BAD_REQUEST));
			}
			return commentClient.createRootComment(root)
					.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
							.map(enriched -> responseFactory.ok(enriched, req, HttpStatus.CREATED))
					);
		} else {
			ReplyCreateRequest reply = new ReplyCreateRequest();
			reply.setWriterId(userId);
			reply.setContents(request.getContents());
			return commentClient.createReply(parentId, reply)
					.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
							.map(enriched -> responseFactory.ok(enriched, req, HttpStatus.CREATED))
					);
		}
	}
	
	
	// 3) 특정 아티클의 전체 댓글 조회
	@Operation(summary = "특정 아티클의 전체 댓글 조회(10개씩)",
			description = "특정 아티클의 전체 댓글을 조회한합니다., mode = all : 전체 댓글 조회, mode=visibleCount , 페이징 처리, 1부터 시작하면 됩니다.(게시즐 조회시 0번 페이지 조회) ")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "CommentsByArticle200", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [\n    {\n      \"commentId\": \"c1\",\n      \"articleId\": \"{articleId}\",\n      \"writerId\": \"user-1\",\n      \"parentCommentId\": null,\n      \"rootCommentId\": \"c1\",\n      \"depth\": 0,\n      \"contents\": \"내용\",\n      \"isDeleted\": false,\n      \"status\": \"ACTIVE\",\n      \"replyCount\": 0,\n      \"createdAt\": \"2025-10-03T10:23:45Z\",\n      \"updatedAt\": \"2025-10-03T10:23:45Z\",\n      \"deletedAt\": null\n    }\n  ],\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/article/{articleId}\"\n  }\n}")))
	})
	@GetMapping("/article")
	public Mono<ResponseEntity<BaseResponse>> getByArticle(@RequestParam String articleId,
	                                                       @RequestParam(required = false, defaultValue = "0") Integer page,
	                                                       @RequestParam(required = false, defaultValue = "visibleCount") String mode,
	                                                       ServerHttpRequest req) {
		return commentClient.getCommentsByArticle(articleId, page, 10, mode)
				.flatMap(result -> profileEnrichmentUtil.enrichAny(result)
						.map(enriched -> responseFactory.ok(enriched, req))
				);
	}

//    // 4) 특정 부모의 대댓글 목록 조회
//    @Operation(summary = "특정 부모의 대댓글 목록 조회")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "성공",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "RepliesByParent200", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [ { \n    \"commentId\": \"c2\", \n    \"articleId\": \"article-1\",\n    \"writerId\": \"user-2\",\n    \"parentCommentId\": \"c1\",\n    \"rootCommentId\": \"c1\",\n    \"depth\": 1,\n    \"contents\": \"대댓글 내용\",\n    \"isDeleted\": false,\n    \"status\": \"ACTIVE\",\n    \"replyCount\": 0,\n    \"createdAt\": \"2025-10-03T10:25:00Z\",\n    \"updatedAt\": \"2025-10-03T10:25:00Z\",\n    \"deletedAt\": null\n  } ],\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{parentId}/replies\"\n  }\n}")))
//    })
//    @GetMapping("/{parentId}/replies")
//    public Mono<ResponseEntity<BaseResponse>> getReplies(@PathVariable String parentId, ServerHttpRequest req) {
//        return commentClient.getReplies(parentId)
//                .map(result -> responseFactory.ok(result, req));
//    }

//    // 5) 루트 댓글 기준 스레드 전체 조회
//    @Operation(summary = "루트 댓글 기준 스레드 전체 조회")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "성공",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "ThreadByRoot200", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": [ { \"commentId\": \"c1\" }, { \"commentId\": \"c2\" } ],\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/thread/{rootId}\"\n  }\n}")))
//    })
//    @GetMapping("/thread/{rootId}")
//    public Mono<ResponseEntity<BaseResponse>> getThread(@PathVariable String rootId, ServerHttpRequest req) {
//        return commentClient.getThread(rootId)
//                .map(result -> responseFactory.ok(result, req));
//    }

//    // 6) 단건 조회
//    @Operation(summary = "댓글 단건 조회")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "성공",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "GetOneComment200", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \n    \"commentId\": \"c1\", \n    \"articleId\": \"article-1\",\n    \"writerId\": \"user-1\",\n    \"parentCommentId\": null,\n    \"rootCommentId\": \"c1\",\n    \"depth\": 0,\n    \"contents\": \"내용\",\n    \"isDeleted\": false,\n    \"status\": \"ACTIVE\",\n    \"replyCount\": 0,\n    \"createdAt\": \"2025-10-03T10:23:45Z\",\n    \"updatedAt\": \"2025-10-03T10:23:45Z\",\n    \"deletedAt\": null\n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}"))),
//            @ApiResponse(responseCode = "404", description = "미존재",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "GetOneNotFound404", value = "{\n  \"isSuccess\": false,\n  \"code\": 404,\n  \"data\": \"댓글을 찾을 수 없습니다.\",\n  \"request\": { \n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}")))
//    })
//    @GetMapping("/{id}")
//    public Mono<ResponseEntity<BaseResponse>> getOne(@PathVariable String id, ServerHttpRequest req) {
//        return commentClient.getOne(id)
//                .map(result -> responseFactory.ok(result, req));
//    }
	
	// 7) 댓글 내용 수정
	@Operation(summary = "댓글 내용 수정",
			description = "작성자 본인만 수정 가능. 내용이 비어있으면 400 반환.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "UpdateComment200", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \n    \"commentId\": \"c1\", \n    \"contents\": \"수정한 내용\" \n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}"))),
			@ApiResponse(responseCode = "403", description = "본인 아님",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "UpdateComment403", value = "{\n  \"isSuccess\": false,\n  \"code\": 403,\n  \"data\": \"작성자 본인만 댓글을 수정/삭제할 수 있습니다.\",\n  \"request\": { \n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}"))),
			@ApiResponse(responseCode = "400", description = "내용 비어있음",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "UpdateComment400", value = "{\n  \"isSuccess\": false,\n  \"code\": 400,\n  \"data\": \"댓글 내용은 비어 있을 수 없습니다.\",\n  \"request\": { \n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}"))),
			@ApiResponse(responseCode = "404", description = "미존재",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "UpdateComment404", value = "{\n  \"isSuccess\": false,\n  \"code\": 404,\n  \"data\": \"댓글을 찾을 수 없습니다.\",\n  \"request\": { \n    \"path\": \"/bff/v1/communities/comments/{id}\"\n  }\n}")))
	})
	@PatchMapping("/{id}")
	public Mono<ResponseEntity<BaseResponse>> update(@PathVariable String id,
	                                                 @RequestBody CommentUpdateRequest request,
	                                                 ServerHttpRequest req) {
		// 토큰에서 userId 추출하여 설정
		String userId = userIdValidator.extractTokenUserId(req);
		request.setWriterId(userId);
		
		return commentClient.update(id, request)
				.map(result -> responseFactory.ok(result, req));
	}
	
	// 8) 소프트 삭제
	@Operation(summary = "댓글 소프트 삭제",
			description = "작성자 본인만 가능. 성공 시 204(No Content)와 함께 data는 null입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제됨",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SoftDelete204", value = "{\n  \"isSuccess\": true,\n  \"code\": 204,\n  \"data\": null,\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}?writerId=user-1\"\n  }\n}"))),
			@ApiResponse(responseCode = "403", description = "본인 아님",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SoftDelete403", value = "{\n  \"isSuccess\": false,\n  \"code\": 403,\n  \"data\": \"작성자 본인만 댓글을 수정/삭제할 수 있습니다.\",\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}?writerId=other\"\n  }\n}"))),
			@ApiResponse(responseCode = "404", description = "미존재",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = BaseResponse.class),
							examples = @ExampleObject(name = "SoftDelete404", value = "{\n  \"isSuccess\": false,\n  \"code\": 404,\n  \"data\": \"댓글을 찾을 수 없습니다.\",\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/{id}?writerId=user-1\"\n  }\n}")))
	})
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<BaseResponse>> softDelete(@PathVariable String id,
	                                                     ServerHttpRequest req) {
		// 토큰에서 userId 추출
		String userId = userIdValidator.extractTokenUserId(req);
		
		return commentClient.softDelete(id, userId)
				.thenReturn(responseFactory.ok(null, req, HttpStatus.NO_CONTENT));
	}

//    // 9) 여러 게시글에 대한 댓글 수 조회
//    @Operation(summary = "여러 게시글의 댓글 수 조회",
//            description = "body에 articleId 리스트를 전달하면 각 articleId에 대한 댓글 수를 반환합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "성공",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = BaseResponse.class),
//                            examples = @ExampleObject(name = "CountsForArticles200", value = "{\n  \"isSuccess\": true,\n  \"code\": 200,\n  \"data\": { \n    \"article-1\": 3, \n    \"article-2\": 0 \n  },\n  \"request\": {\n    \"path\": \"/bff/v1/communities/comments/articles/counts\"\n  }\n}")))
//    })
//    @PostMapping("/articles/counts")
//    public Mono<ResponseEntity<BaseResponse>> getCountsForArticles(@RequestBody List<String> articleIds, ServerHttpRequest req) {
//        if (articleIds == null || articleIds.isEmpty()) {
//            return Mono.just(responseFactory.ok(java.util.Map.of(), req));
//        }
//        return commentClient.getCountsForArticles(articleIds)
//                .map(counts -> responseFactory.ok(counts, req));
//    }
}
