package com.study.api_gateway.controller;

import com.study.api_gateway.api.auth.controller.AuthController;
import com.study.api_gateway.api.auth.dto.request.LoginRequest;
import com.study.api_gateway.api.auth.dto.response.LoginResponse;
import com.study.api_gateway.api.auth.service.AuthFacadeService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = AuthController.class)
@Import({ResponseFactory.class, RequestPathHelper.class, GlobalExceptionHandler.class, com.study.api_gateway.config.TestConfig.class})
@Disabled("Needs refactoring to match current architecture")
class AuthControllerPathInjectionTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private AuthFacadeService authFacadeService;

	@Test
	@DisplayName("X-Original-URI 헤더가 있으면 응답.request.path/url 에 그대로 반영된다")
	void pathInjectedFromOriginalHeader() {
		// given
		LoginResponse resp = LoginResponse.builder()
				.accessToken("access")
				.refreshToken("refresh")
				.deviceId("d")
				.build();

		Mockito.when(authFacadeService.login("a@a.com", "pwd"))
				.thenReturn(Mono.just(resp));

		// when - then
		webTestClient.post()
				.uri("/bff/v1/auth/login")
				.header("X-Original-URI", "/v1/auth/login")
				.header("X-Forwarded-Proto", "https")
				.header("X-Forwarded-Host", "api.example.com")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"email\":\"a@a.com\",\"password\":\"pwd\"}")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.isSuccess").isEqualTo(true)
				.jsonPath("$.code").isEqualTo(200)
				.jsonPath("$.request.path").isEqualTo("/v1/auth/login")
				.jsonPath("$.request.url").isEqualTo("https://api.example.com/v1/auth/login");
	}

	@Test
	@DisplayName("헤더가 없으면 /bff 프리픽스가 제거되어 응답.request.path/url 에 반영된다")
	void pathFallbackByStrippingBff() {
		// given
		LoginResponse resp = LoginResponse.builder()
				.accessToken("access")
				.refreshToken("refresh")
				.deviceId("d")
				.build();
		Mockito.when(authFacadeService.login("a@a.com", "pwd"))
				.thenReturn(Mono.just(resp));

		// when - then
		webTestClient.post()
				.uri("/bff/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"email\":\"a@a.com\",\"password\":\"pwd\"}")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.isSuccess").isEqualTo(true)
				.jsonPath("$.code").isEqualTo(200)
				.jsonPath("$.request.path").isEqualTo("/v1/auth/login")
				.jsonPath("$.request.url").isEqualTo("http://localhost/v1/auth/login");
	}
}
