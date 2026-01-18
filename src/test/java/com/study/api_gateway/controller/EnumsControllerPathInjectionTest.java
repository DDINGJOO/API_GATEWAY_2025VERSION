package com.study.api_gateway.controller;

import com.study.api_gateway.aggregation.enums.controller.EnumsController;
import com.study.api_gateway.api.article.client.ArticleClient;
import com.study.api_gateway.api.auth.client.AuthClient;
import com.study.api_gateway.api.image.client.ImageClient;
import com.study.api_gateway.api.place.client.PlaceClient;
import com.study.api_gateway.api.profile.client.ProfileClient;
import com.study.api_gateway.api.room.client.RoomClient;
import com.study.api_gateway.api.support.client.FaqClient;
import com.study.api_gateway.common.exception.GlobalExceptionHandler;
import com.study.api_gateway.common.response.ResponseFactory;
import com.study.api_gateway.common.util.RequestPathHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@WebFluxTest(controllers = EnumsController.class)
@Import({ResponseFactory.class, RequestPathHelper.class, GlobalExceptionHandler.class, com.study.api_gateway.config.TestConfig.class})
@Disabled("Needs refactoring to match current architecture")
class EnumsControllerPathInjectionTest {
	
	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	private ProfileClient profileClient;
	
	@MockBean
	private AuthClient authClient;
	
	@MockBean
	private ImageClient imageClient;
	
	@MockBean
	private ArticleClient articleClient;
	
	@MockBean
	private FaqClient faqClient;
	
	@MockBean
	private PlaceClient placeClient;
	
	@MockBean
	private RoomClient roomClient;
	
	@Test
	@DisplayName("X-Forwarded-Prefix + X-Forwarded-Uri 헤더가 결합되어 응답.request.path/url 에 반영된다")
	void pathInjectedFromForwardedHeaders() {
		Mockito.when(profileClient.fetchGenres())
				.thenReturn(Mono.just(Map.of(1, "Rock")));
		
		webTestClient.get()
				.uri("/bff/v1/enums/genres")
				.header("X-Forwarded-Prefix", "/gateway")
				.header("X-Forwarded-Uri", "/v1/enums/genres?lang=ko")
				.header("X-Forwarded-Proto", "http")
				.header("X-Forwarded-Host", "gw.example.com")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.request.path").isEqualTo("/gateway/v1/enums/genres?lang=ko")
				.jsonPath("$.request.url").isEqualTo("http://gw.example.com/gateway/v1/enums/genres?lang=ko");
	}
}
