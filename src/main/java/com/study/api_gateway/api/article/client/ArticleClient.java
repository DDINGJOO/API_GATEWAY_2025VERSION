package com.study.api_gateway.api.article.client;


import com.study.api_gateway.api.article.dto.request.ArticleCreateRequest;
import com.study.api_gateway.api.article.dto.response.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Article Server와 통신하는 WebClient 기반 클라이언트
 * - 게시글, 이벤트, 공지사항 CRUD 제공
 * - Enum 조회 (게시판, 키워드)
 */
@Component
public class ArticleClient {
	private final WebClient webClient;
	private final String PREFIX = "/api/v1";
	
	public ArticleClient(@Qualifier(value = "articleWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	// ==================== 일반 게시글 API ====================
	
	/**
	 * 게시글 생성
	 * POST /api/v1/articles
	 */
	public Mono<ArticleResponse> postArticle(ArticleCreateRequest request) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/articles")
				.toUriString();
		
		return webClient.post()
				.uri(uriString)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	/**
	 * 게시글 단건 조회
	 * GET /api/v1/articles/{articleId}
	 */
	public Mono<ArticleResponse> getArticle(String articleId) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/articles/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	/**
	 * 게시글 삭제 (소프트 삭제)
	 * DELETE /api/v1/articles/{articleId}
	 */
	public Mono<Void> deleteArticle(String articleId) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/articles/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.delete()
				.uri(uriString)
				.retrieve()
				.bodyToMono(Void.class);
	}
	
	/**
	 * 게시글 수정
	 * PUT /api/v1/articles/{articleId}
	 */
	public Mono<ArticleResponse> updateArticle(String articleId, ArticleCreateRequest req) {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/articles/{articleId}")
				.buildAndExpand(articleId)
				.toUriString();
		
		return webClient.put()
				.uri(uriString)
				.bodyValue(req)
				.retrieve()
				.bodyToMono(ArticleResponse.class);
	}
	
	/**
	 * 게시글 검색 (커서 페이지네이션)
	 * GET /api/v1/articles/search
	 */
	public Mono<ArticleCursorPageResponse> fetchArticleCursorPageResponse(
			Integer size, String cursorId, Long boardIds, List<Long> keyword,
			String title, String content, String writerId) {
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/articles/search");
		if (size != null) builder.queryParam("size", size);
		if (cursorId != null && !cursorId.isBlank()) builder.queryParam("cursorId", cursorId);
		if (boardIds != null) builder.queryParam("boardIds", boardIds);
		if (keyword != null && !keyword.isEmpty()) builder.queryParam("keyword", keyword);
		if (title != null && !title.isBlank()) builder.queryParam("title", title);
		if (content != null && !content.isBlank()) builder.queryParam("content", content);
		if (writerId != null && !writerId.isBlank()) builder.queryParam("writerId", writerId);
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(ArticleCursorPageResponse.class);
	}
	
	// ==================== Bulk API ====================
	
	/**
	 * 게시글 대량 조회
	 * GET /api/v1/bulk/articles?ids={id1}&ids={id2}
	 *
	 * @param ids 게시글 ID 리스트
	 * @return 게시글 간단 정보 리스트
	 */
	public Mono<List<ArticleSimpleResponse>> getBulkArticles(List<String> ids) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(PREFIX + "/bulk/articles");
		
		// Add each id as a separate query parameter
		if (ids != null && !ids.isEmpty()) {
			ids.forEach(id -> builder.queryParam("ids", id));
		}
		
		String uriString = builder.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<ArticleSimpleResponse>>() {
				});
	}
	
	// ==================== Enums API ====================
	
	/**
	 * 게시판 목록 조회
	 * GET /api/v1/enums/boards
	 *
	 * @return key: 게시판 ID (문자열), value: 게시판 정보 (id, name)
	 */
	public Mono<Map<String, BoardEnumDto>> getBoards() {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/boards")
				.toUriString();
		
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, BoardEnumDto>>() {
				});
	}
	
	/**
	 * 키워드 목록 조회
	 * GET /api/v1/enums/keywords
	 *
	 * @return key: 키워드 ID (문자열), value: 키워드 정보 (id, name)
	 */
	public Mono<Map<String, KeywordEnumDto>> getKeywords() {
		String uriString = UriComponentsBuilder.fromPath(PREFIX + "/enums/keywords")
				.toUriString();
		return webClient.get()
				.uri(uriString)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, KeywordEnumDto>>() {
				});
	}
}
