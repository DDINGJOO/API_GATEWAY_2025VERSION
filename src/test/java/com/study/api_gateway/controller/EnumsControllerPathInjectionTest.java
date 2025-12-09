package com.study.api_gateway.controller;

import com.study.api_gateway.client.AuthClient;
import com.study.api_gateway.client.ImageClient;
import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.config.GlobalExceptionHandler;
import com.study.api_gateway.controller.enums.EnumsController;
import com.study.api_gateway.util.RequestPathHelper;
import com.study.api_gateway.util.ResponseFactory;
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
class EnumsControllerPathInjectionTest {
	
	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	private ProfileClient profileClient;
	
	@MockBean
	private AuthClient authClient;
	
	@MockBean
	private ImageClient imageClient;
	
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
