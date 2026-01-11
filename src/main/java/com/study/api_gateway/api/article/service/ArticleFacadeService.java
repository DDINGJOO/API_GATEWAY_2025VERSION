package com.study.api_gateway.api.article.service;

import com.study.api_gateway.api.article.client.ArticleClient;
import com.study.api_gateway.api.article.client.EventClient;
import com.study.api_gateway.api.article.client.NoticeClient;
import com.study.api_gateway.api.article.dto.request.ArticleCreateRequest;
import com.study.api_gateway.api.article.dto.response.*;
import com.study.api_gateway.common.resilience.ResilienceOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Article 도메인 Facade Service
 * Controller와 Client 사이의 중간 계층으로 Resilience 패턴 적용
 */
@Service
@RequiredArgsConstructor
public class ArticleFacadeService {

	private final ArticleClient articleClient;
	private final EventClient eventClient;
	private final NoticeClient noticeClient;
	private final ResilienceOperator resilience;

	private static final String SERVICE_NAME = "article-service";

	// ==================== 일반 게시글 API ====================

	public Mono<ArticleResponse> postArticle(ArticleCreateRequest request) {
		return articleClient.postArticle(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ArticleResponse> getArticle(String articleId) {
		return articleClient.getArticle(articleId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Void> deleteArticle(String articleId) {
		return articleClient.deleteArticle(articleId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ArticleResponse> updateArticle(String articleId, ArticleCreateRequest req) {
		return articleClient.updateArticle(articleId, req)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ArticleCursorPageResponse> fetchArticleCursorPageResponse(
			Integer size, String cursorId, Long boardIds, List<Long> keyword,
			String title, String content, String writerId) {
		return articleClient.fetchArticleCursorPageResponse(size, cursorId, boardIds, keyword, title, content, writerId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<List<ArticleSimpleResponse>> getBulkArticles(List<String> ids) {
		return articleClient.getBulkArticles(ids)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Map<String, BoardEnumDto>> getBoards() {
		return articleClient.getBoards()
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Map<String, KeywordEnumDto>> getKeywords() {
		return articleClient.getKeywords()
				.transform(resilience.protect(SERVICE_NAME));
	}

	// ==================== 이벤트 API ====================

	public Mono<EventArticleResponse> postEvent(ArticleCreateRequest request) {
		return eventClient.postEvent(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<EventArticleResponse> getEvent(String articleId) {
		return eventClient.getEvent(articleId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Void> deleteEvent(String articleId) {
		return eventClient.deleteEvent(articleId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<EventArticleResponse> updateEvent(String articleId, ArticleCreateRequest req) {
		return eventClient.updateEvent(articleId, req)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Object> getEvents(String status, Integer page, Integer size) {
		return eventClient.getEvents(status, page, size)
				.transform(resilience.protect(SERVICE_NAME));
	}

	// ==================== 공지사항 API ====================

	public Mono<ArticleResponse> postNotice(ArticleCreateRequest request) {
		return noticeClient.postNotice(request)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ArticleResponse> getNotice(String articleId) {
		return noticeClient.getNotice(articleId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Void> deleteNotice(String articleId) {
		return noticeClient.deleteNotice(articleId)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<ArticleResponse> updateNotice(String articleId, ArticleCreateRequest req) {
		return noticeClient.updateNotice(articleId, req)
				.transform(resilience.protect(SERVICE_NAME));
	}

	public Mono<Object> getNotices(Integer page, Integer size) {
		return noticeClient.getNotices(page, size)
				.transform(resilience.protect(SERVICE_NAME));
	}
}
