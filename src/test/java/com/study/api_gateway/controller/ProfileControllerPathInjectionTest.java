package com.study.api_gateway.controller;

import com.study.api_gateway.client.ProfileClient;
import com.study.api_gateway.config.GlobalExceptionHandler;
import com.study.api_gateway.controller.profile.ProfileController;
import com.study.api_gateway.dto.profile.response.UserResponse;
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

@WebFluxTest(controllers = ProfileController.class)
@Import({ResponseFactory.class, RequestPathHelper.class, GlobalExceptionHandler.class, com.study.api_gateway.config.TestConfig.class})
class ProfileControllerPathInjectionTest {
	
	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	private ProfileClient profileClient;
	
	@MockBean
	private com.study.api_gateway.service.ImageConfirmService imageConfirmService;
	
	@Test
	@DisplayName("프로필 단건 조회: /bff 프리픽스 제거되어 응답.request.path/url 에 반영")
	void fetchProfilePathFallback() {
		Mockito.when(profileClient.fetchProfile("u1"))
				.thenReturn(Mono.just(UserResponse.builder().userId("u1").build()));
		
		webTestClient.get()
				.uri("/bff/v1/profiles/u1")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.request.path").isEqualTo("/v1/profiles/u1")
				.jsonPath("$.request.url").isEqualTo("http://localhost/v1/profiles/u1");
	}
}
